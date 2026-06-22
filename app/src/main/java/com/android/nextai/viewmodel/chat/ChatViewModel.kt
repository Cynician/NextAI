package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.data.datasource.remote.ApiType
import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.MessageType
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent
import com.android.nextai.domain.usecase.chat.CreateMessageUseCase
import com.android.nextai.domain.usecase.chat.CreateSessionWithUserMessageUseCase
import com.android.nextai.domain.usecase.chat.GetLastPageMessagesUseCase
import com.android.nextai.domain.usecase.chat.StreamingGenUseCase
import com.android.nextai.domain.usecase.chat.UpdateMessageUseCase
import com.android.nextai.ui.component.markdown.parser.MarkdownIncrementalParser
import com.android.nextai.viewmodel.chat.holder.ChatSessionHolder
import com.android.nextai.viewmodel.chat.holder.MarkdownCacheHolder
import com.android.nextai.viewmodel.chat.state.ChatSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ChatViewModel @Inject constructor(
    val sessionHolder: ChatSessionHolder,
    val markdownCacheHolder: MarkdownCacheHolder,
    private val createSessionWithUserMessageUseCase: CreateSessionWithUserMessageUseCase,
    private val createMessageUseCase: CreateMessageUseCase,
    private val streamingGenUseCase: StreamingGenUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val getLastPageMessagesUseCase: GetLastPageMessagesUseCase,
) : ViewModel() {
    companion object {
        private const val TAG = "ChatViewModel"
    }

    val generationJobs = ConcurrentHashMap<Long, Job>()
    private val loadMessagesJobs = ConcurrentHashMap<Long, Job>()

    init {
        viewModelScope.launch {
            try {
                sessionHolder.loadingSessions()
            } catch (e: Exception) {
                Log.e(TAG, "Could not load sessions: $e")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSessionState: StateFlow<ChatSessionState?> = sessionHolder.curSessionId
        .flatMapLatest { id ->
            sessionHolder.sessionStates.map { allStates -> allStates[id] }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Get ai response
     */
    fun sendUserQuery(
        query: String,
        provider: Provider,
    ) {
        val isSportStreamingGen = true
        val isInSession = sessionHolder.isInSession
        val activeSessionId = sessionHolder.getCurSessionId()
        var targetSessionId = activeSessionId
        val modelId = provider.models.first().id
        if (isInSession) {
            generationJobs[activeSessionId]?.cancel()
        }

        // 1. Starts a separate backend job pipeline for this session.
        val job = viewModelScope.launch {
            try {
                var userMessage: Message

                // 2. Preprocessing: If not in a Session, it means a new session needs to be created.
                if (!isInSession) {
                    Log.d(TAG, "create session with query :$query")
                    userMessage =
                        createSessionWithUserMessageUseCase(
                            query,
                            provider,
                            modelId
                        ).getOrThrow().second

                    // Get true session id then update
                    targetSessionId = userMessage.sessionId
                    generationJobs[targetSessionId] = coroutineContext[Job]!!

                    // Set the new session to the current global active state.
                    sessionHolder.updateIsInSession(true)
                    sessionHolder.updateCurSessionId(targetSessionId)
                    sessionHolder.updateCurMessagesMinId(targetSessionId, userMessage.id)
                } else {
                    // 3. Already in the session, append the message under this targetSessionId.
                    userMessage = createMessageUseCase(
                        sessionId = targetSessionId,
                        providerName = provider.name,
                        modelId = modelId,
                        content = query,
                        messageType = MessageType.USER
                    ).getOrThrow()
                }

                sessionHolder.clearCurrentResponse(targetSessionId)
                sessionHolder.loadingSessions()
                sessionHolder.addMessage(targetSessionId, userMessage)

                // Only when the target session matches the global selected session.
                if (targetSessionId == sessionHolder.getCurSessionId()) {
                    sessionHolder.emitScrollToLatestMessageEvent(targetSessionId)
                }

                // 5. Start streaming generation in the background.
                if (isSportStreamingGen) {
                    startStreamingGen(targetSessionId, provider, modelId)
                }
            } catch (e: CancellationException) {
                Log.e(TAG, "job cancel", e)
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}: ", e)
                withContext(NonCancellable) {
                    try {
                        val errorMessage = createMessageUseCase(
                            sessionId = targetSessionId,
                            content = e.message.toString(),
                            messageType = MessageType.ERROR
                        ).getOrThrow()

                        sessionHolder.clearCurrentResponse(targetSessionId)
                        sessionHolder.loadingSessions()
                        sessionHolder.addMessage(targetSessionId, errorMessage)
                    } catch (e: Exception) {
                        Log.e(TAG, "create error message failed: $e")
                    }
                }
            } finally {
                // Always clear the job reference for the real target session so a stale
                // entry never blocks subsequent sends (e.g. after "new session" the
                // activeSessionId was -1L, which previously polluted generationJobs[-1L]
                // and made the bottom bar think a generation was still active).
                generationJobs.remove(targetSessionId)
            }
        }
        // Register the job under the REAL target session id. Previously this used
        // `activeSessionId`, which for a brand-new session was -1L and polluted the
        // map with a -1L -> job entry.
        if (isInSession) {
            generationJobs[targetSessionId] = job
        }
    }

    /**
     * Backend stream generation core function.
     */
    private suspend fun startStreamingGen(
        sessionId: Long,
        provider: Provider,
        modelId: String,
    ) {
        Log.d(TAG, "startStreamingGen# session: $sessionId")
        sessionHolder.updateIsTextStreaming(sessionId, true)

        val assistantMessage = createMessageUseCase(
            sessionId = sessionId,
            providerName = provider.name,
            modelId = modelId,
            content = "",
            messageType = MessageType.ASSISTANT
        ).getOrThrow()
        val parser = markdownCacheHolder.getOrCreate(assistantMessage.id)
        sessionHolder.addMessage(sessionId, assistantMessage)

        if (sessionId == sessionHolder.getCurSessionId()) {
            sessionHolder.emitScrollToLatestMessageEvent(sessionId)
        }

        streamingGenUseCase(
            apiType = ApiType.OPENAI,
            messageList = sessionHolder.getState(sessionId).messageList,
            provider = provider,
            modelId = modelId,
            onCancel = { streamingCache ->
                viewModelScope.launch {
                    onStreamingCancelCallBack(
                        streamingCache = streamingCache,
                        assistantMessage = assistantMessage,
                        parser = parser
                    )
                }
            }
        ).getOrThrow().collect { event ->
            withContext(NonCancellable){
                when (event) {
                    is GenerationEvent.Chunk -> {
                        onStreamingReceiveChunk(assistantMessage, parser, event.content)
                    }

                    is GenerationEvent.Done -> {
                        onStreamingDone(parser, assistantMessage)
                    }

                    is GenerationEvent.Error -> {
                        sessionHolder.updateIsTextStreaming(sessionId, false)
                        generationJobs.remove(sessionId)
                    }
                }
            }

        }
    }

    private fun onStreamingReceiveChunk(assistantMessage:Message, parser: MarkdownIncrementalParser, chunk: String){
        val sessionId = assistantMessage.sessionId
        sessionHolder.updateCurResponse(sessionId, chunk)
        val content = sessionHolder.getState(sessionId).curResponse
        parser.appendDelta(chunk)
        // Refresh the last message in the dedicated memory map of the session in real
        // time. If the user switches back to the session, the UI will see text ticking
        // in real time.
        sessionHolder.updateLastestMessage(
            sessionId, assistantMessage.copy(content = content)
        )
    }

    private suspend fun onStreamingDone(parser: MarkdownIncrementalParser, assistantMessage:Message){
        parser.complete()
        val sessionId = assistantMessage.sessionId
        updateMessageUseCase(
            id = assistantMessage.id,
            sessionId = sessionId,
            content = sessionHolder.getState(sessionId).curResponse
        ).getOrThrow()
        sessionHolder.updateIsTextStreaming(sessionId, false)
        sessionHolder.loadingSessions()
        generationJobs.remove(sessionId) // Task completes normally, removing the Job reference.
    }

    private suspend fun onStreamingCancelCallBack(
        streamingCache: String,
        assistantMessage: Message,
        parser: MarkdownIncrementalParser,
    ){
        Log.d(TAG, "onStreamingCancelCallBack# cache size: ${streamingCache.length}")
        val sessionId = assistantMessage.sessionId
        val currentUiText = sessionHolder.getState(sessionId).curResponse
        if (streamingCache.length > currentUiText.length && streamingCache.startsWith(currentUiText)) {
            val deltaText = streamingCache.substring(currentUiText.length)
            parser.appendDelta(deltaText)
            parser.complete()
            sessionHolder.updateCurResponse(sessionId, deltaText)
        }
        sessionHolder.updateLastestMessage(
            sessionId, assistantMessage.copy(content = streamingCache)
        )
        updateMessageUseCase(
            id = assistantMessage.id,
            sessionId = sessionId,
            content = streamingCache
        ).onFailure {
            Log.d(TAG,it.message, it)
        }
    }

    /**
     * Stop the generation task for the current session.
     * Cancels the coroutine, saves partial response to database, and updates state.
     */
    fun stopStreamingGen(sessionId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "stopStreamingGen#, stop streaming generating, sessionId: $sessionId")
                val job = generationJobs[sessionId] ?: return@launch
                if (!job.isActive) return@launch

                // Cancel the streaming coroutine
                job.cancel()

                // Save the partially generated response to database
                val state = sessionHolder.getState(sessionId)
                val messageList = state.messageList
                if (messageList.isNotEmpty() && state.curResponse.isNotEmpty()) {
                    val lastMessage = messageList.last()
                    if (lastMessage.type == MessageType.ASSISTANT) {
                        updateMessageUseCase(
                            id = lastMessage.id,
                            sessionId = sessionId,
                            content = state.curResponse
                        )
                    }
                }

                // Update streaming state
                sessionHolder.updateIsTextStreaming(sessionId, false)
                generationJobs.remove(sessionId)
            } catch (e: Exception) {
                Log.e(TAG, "stopGeneration# error: ", e)
            }
        }
    }

    /**
     * Session init
     */
    fun initSession() {
        // Clear any stale job reference keyed by the "no session" id (-1L).
        // sendUserQuery previously registered its job under activeSessionId which could
        // be -1L for a brand-new session, leaving a ghost entry that made the bottom
        // bar believe a generation was still running on the empty/new-session screen.
        generationJobs.remove(-1L)
        sessionHolder.initSession()
    }

    /**
     * Batch operation
     */
    fun batchDeleteSessions(idList: List<Long>) {
        viewModelScope.launch {
            try {
                if (idList.contains(sessionHolder.getCurSessionId())) {
                    initSession()
                }
                sessionHolder.onBatchDeleteSessions(true, idList)
                idList.forEach { id ->
                    sessionHolder.removeSession(id)

                    // 3. Should cut off their streaming or load messages jobs that might still
                    // be looping in the background.
                    generationJobs[id]?.cancel()
                    generationJobs.remove(id)
                    loadMessagesJobs[id]?.cancel()
                    loadMessagesJobs.remove(id)
                }
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}", e)
            }

        }
    }

    fun batchPinSessions(idList: List<Long>) {
        viewModelScope.launch {
            try {
                sessionHolder.onBatchPinSessions(idList)
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}", e)
            }
        }
    }

    fun batchUnpinSessions(idList: List<Long>) {
        viewModelScope.launch {
            try {
                sessionHolder.onBatchUnpinSessions(idList)
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}", e)
            }
        }
    }

    /**
     * Paging to pull historical messages (fully supports sessionId level isolation and independent pagination progress).
     */
    fun loadMessages(
        sessionId: Long,
        isLoadingFirst: Boolean,
        callBack: (() -> Unit)? = null,
    ) {
        loadMessagesJobs[sessionId]?.cancel()
        loadMessagesJobs[sessionId] = viewModelScope.launch {
            try {
                // Wait for a few frames to render loading UI before update loading state, other wise,
                // the state changed instantly, the loading UI have no time to render.
                if(isLoadingFirst) repeat(6){awaitFrame()}
                val sessionState = sessionHolder.getState(sessionId)

                val messageList = getLastPageMessagesUseCase(
                    sessionId = sessionId,
                    minMsgId = sessionState.curMessagesMinId
                ).getOrThrow()

                if (messageList.isNotEmpty()) {
                    messageList.forEach { message ->
                        if (message.type == MessageType.ASSISTANT) {
                            val cached = markdownCacheHolder.get(message.id)
                            if (cached == null) {
                                val parser = markdownCacheHolder.getOrCreate(message.id)
                                parser.setContent(message.content)
                            }
                        }
                    }

                    //Update the minimum message ID progress specific to this session.
                    sessionHolder.updateCurMessagesMinId(sessionId, messageList.first().id)

                    if (isLoadingFirst) {
                        sessionHolder.setMessages(sessionId, messageList)
                    } else {
                        sessionHolder.addMessagesToHead(sessionId, messageList)
                    }
                    Log.d(TAG, "loadMessages# session:$sessionId size:${messageList.size}")
                } else {
                    // No more messages of current session.
                    sessionHolder.updateHasMoreMessages(sessionId, false)
                }
                if (callBack != null) callBack()
            } finally {
                loadMessagesJobs.remove(sessionId)
                if(isLoadingFirst) sessionHolder.updateIsLoadingFirst(false)
            }
        }
    }

    /**
     * Handles the logic when a user clicks a sidebar session, updates status,
     * and loading paginated messages。
     */
    fun onChangingSession(sessionId: Long) {

        sessionHolder.updateIsLoadingFirst(true)
        val cachedMessages = sessionHolder.getState(sessionId).messageList

        if (cachedMessages.isNotEmpty()) {
            viewModelScope.launch {
                // Wait for a few frames to render loading UI before update loading state, other wise,
                // the state changed instantly, the loading UI have no time to render.
                repeat(6) { awaitFrame() }
                sessionHolder.updateIsLoadingFirst(false)
                sessionHolder.onLoadMessagesSuccess(sessionId)
            }
        } else {
            loadMessages(
                sessionId = sessionId,
                isLoadingFirst = true,
                callBack = {
                    sessionHolder.onLoadMessagesSuccess(sessionId)
                }
            )
        }
    }

    /**
     * Swipe up to the top to load the next page.
     */
    fun loadLastPageMessages() {
        val currentSessionId = sessionHolder.getCurSessionId()
        val sessionState = sessionHolder.getState(currentSessionId)

        if (sessionState.canLoadingMore || !sessionHolder.isInSession) {
            Log.d(TAG, "loadLastPageMessages# skip to load messages")
            return
        }

        Log.d(TAG, "loadLastPageMessages# load next page for $currentSessionId")
        loadMessages(
            sessionId = currentSessionId,
            isLoadingFirst = false,
            callBack = {
                Log.d(TAG, "loadLastPageMessages# success")
            }
        )
    }
}