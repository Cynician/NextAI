package com.android.nextai.viewmodel.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.NextAIApplication
import com.android.nextai.data.datasource.remote.ApiType
import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.MessageType
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent
import com.android.nextai.domain.usecase.chat.CreateMessageUseCase
import com.android.nextai.domain.usecase.chat.CreateSessionWithUserMessageUseCase
import com.android.nextai.domain.usecase.chat.DeleteTailMessagesUseCase
import com.android.nextai.domain.usecase.chat.GetLastPageMessagesUseCase
import com.android.nextai.domain.usecase.chat.StreamingGenUseCase
import com.android.nextai.domain.usecase.chat.UpdateMessageUseCase
import com.android.nextai.service.GenerationForegroundService
import com.android.nextai.ui.component.markdown.parser.MarkdownIncrementalParser
import com.android.nextai.viewmodel.chat.holder.ChatSessionHolder
import com.android.nextai.viewmodel.chat.holder.MarkdownCacheHolder
import com.android.nextai.viewmodel.chat.state.ChatSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val application: Application,
    val sessionHolder: ChatSessionHolder,
    val markdownCacheHolder: MarkdownCacheHolder,
    private val createSessionWithUserMessageUseCase: CreateSessionWithUserMessageUseCase,
    private val createMessageUseCase: CreateMessageUseCase,
    private val streamingGenUseCase: StreamingGenUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val getLastPageMessagesUseCase: GetLastPageMessagesUseCase,
    private val deleteTailMessagesUseCase: DeleteTailMessagesUseCase,
) : ViewModel() {
    companion object {
        private const val TAG = "ChatViewModel"
    }

    /** Application-level scope for tasks that must survive background transitions. */
    private val applicationScope
        get() = (application as NextAIApplication).applicationScope

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
        isRetry: Boolean = false,
    ) {
        val isSportStreamingGen = true
        val isInSession = sessionHolder.isInSession
        val activeSessionId = sessionHolder.getCurSessionId()
        var targetSessionId = activeSessionId
        val modelId = provider.models.first().id
        if (isInSession) {
            generationJobs[activeSessionId]?.cancel()
        }

        // Start foreground service to keep process alive
        GenerationForegroundService.start(application)

        // 1. Starts a separate backend job pipeline for this session.
        val backendJob = applicationScope.launch(Dispatchers.IO) {
            try {
                // 2. Preprocessing: If not in session, it means a new session needs to be created.
                if (!isInSession) {
                    Log.d(TAG, "create session with query :$query")
                    val userMessage =
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
                    sessionHolder.addMessage(targetSessionId, userMessage)
                } else {
                    // 3. Already in the session,append the message under this targetSessionId.
                    if (!isRetry) {
                        val userMessage = createMessageUseCase(
                            sessionId = targetSessionId,
                            providerName = provider.name,
                            modelId = modelId,
                            content = query,
                            messageType = MessageType.USER
                        ).getOrThrow()
                        sessionHolder.addMessage(targetSessionId, userMessage)
                    }
                }

                sessionHolder.clearCurrentResponse(targetSessionId)
                sessionHolder.loadingSessions()

                // Only when the target session matches the global selected session.
                if (targetSessionId == sessionHolder.getCurSessionId()) {
                    sessionHolder.emitScrollToLatestMessageEvent(targetSessionId)
                }

                // 5. Start streaming generation — delegate to applicationScope so the
                //    network call survives background transitions.
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
                // Clear states.
                generationJobs.remove(targetSessionId)
                sessionHolder.updateIsTextStreaming(targetSessionId, false)
                GenerationForegroundService.stop(application)
            }
        }
        // Register the job under the REAL target session id. Previously this used
        // `activeSessionId`, which for a brand-new session was -1L and polluted the
        // map with a -1L -> job entry.
        if (isInSession) {
            generationJobs[targetSessionId] = backendJob
        }
    }

    /**
     * Starts the streaming generation in the applicationScope so that the network
     * connection stays alive even when the app is in the background.
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

        try {
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
                // Bridge events back to viewModelScope for UI-safe updates
                when (event) {
                    is GenerationEvent.Chunk -> {
                        viewModelScope.launch{
                            onStreamingReceiveChunk(assistantMessage, parser, event.content)
                        }
                    }

                    is GenerationEvent.Done -> {
                        onStreamingDone(parser, assistantMessage)
                    }

                    is GenerationEvent.Error -> {
                        sessionHolder.updateIsTextStreaming(sessionId, false)
                        generationJobs.remove(sessionId)
                        throw Exception(event.content)
                    }
                }
            }
        } catch (e: CancellationException) {
            // Cancellation is handled by onCancel callback above, just rethrow
            throw e
        } catch (e: Exception) {
            // Network errors or other exceptions during streaming: ensure state is cleaned up.
            // The outer sendUserQuery catch block will create an error message.
            Log.e(TAG, "startStreamingGen# streaming error: ${e.message}")
            sessionHolder.updateIsTextStreaming(sessionId, false)
            generationJobs.remove(sessionId)
            throw e
        }
    }

    private fun onStreamingReceiveChunk(
        assistantMessage: Message,
        parser: MarkdownIncrementalParser,
        chunk: String,
    ) {
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

    private suspend fun onStreamingDone(
        parser: MarkdownIncrementalParser,
        assistantMessage: Message,
    ) {
        parser.complete()
        val sessionId = assistantMessage.sessionId
        updateMessageUseCase(
            id = assistantMessage.id,
            sessionId = sessionId,
            content = sessionHolder.getState(sessionId).curResponse
        ).onFailure {
            Log.d(TAG, it.message, it)
        }
        sessionHolder.updateIsTextStreaming(sessionId, false)
        sessionHolder.loadingSessions()
        generationJobs.remove(sessionId) // Task completes normally, removing the Job reference.
    }

    private suspend fun onStreamingCancelCallBack(
        streamingCache: String,
        assistantMessage: Message,
        parser: MarkdownIncrementalParser,
    ) {
        Log.d(TAG, "onStreamingCancelCallBack# cache size: ${streamingCache.length}")
        val sessionId = assistantMessage.sessionId
        val currentUiText = sessionHolder.getState(sessionId).curResponse
        if (streamingCache.length > currentUiText.length && streamingCache.startsWith(currentUiText)) {
            val deltaText = streamingCache.substring(currentUiText.length)
            parser.appendDelta(deltaText)
            sessionHolder.updateCurResponse(sessionId, deltaText)
        }
        sessionHolder.updateLastestMessage(
            sessionId, assistantMessage.copy(content = streamingCache)
        )

        onStreamingDone(parser = parser, assistantMessage = assistantMessage)
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

                // Cancel the streaming coroutine (may be in applicationScope)
                job.cancel()

                // Stop foreground service
                GenerationForegroundService.stop(application)

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
     * Delete a single message by id.
     */
    fun deleteMessage(messageId: Long, sessionId: Long) {
        viewModelScope.launch {
            try {
                // Cancel any ongoing generation for this session
                generationJobs[sessionId]?.cancel()
                generationJobs.remove(sessionId)
                sessionHolder.updateIsTextStreaming(sessionId, false)

                // Delete from database
                deleteTailMessagesUseCase(messageId, sessionId)

                // Remove from in-memory state
                sessionHolder.removeTailMessage(sessionId, messageId)

                // Refresh session list
                sessionHolder.loadingSessions()
            } catch (e: Exception) {
                Log.e(TAG, "deleteMessage# error: ", e)
            }
        }
    }

    /**
     * Retry assistant generation: removes the assistant message and resends
     * the preceding user query.
     */
    fun retryAssistantResponse(assistantMessageId: Long, sessionId: Long, provider: Provider) {
        viewModelScope.launch {
            try {
                val state = sessionHolder.getState(sessionId)
                val messageList = state.messageList

                // Find the index of the assistant message
                val assistantIndex = messageList.indexOfFirst { it.id == assistantMessageId }
                if (assistantIndex <= 0) return@launch

                // Find the preceding user message
                messageList.getOrNull(assistantIndex - 1)
                    ?.takeIf { it.type == MessageType.USER }
                    ?: return@launch

                // Cancel any ongoing generation
                generationJobs[sessionId]?.cancel()
                generationJobs.remove(sessionId)

                // Delete the assistant message from database
                deleteTailMessagesUseCase(assistantMessageId, sessionId)

                // Remove the assistant message from state
                sessionHolder.removeTailMessage(sessionId, assistantMessageId)

                // Refresh sidebar so SessionItemView picks up generationJobs changes
                sessionHolder.loadingSessions()

                // Switch to the target session if user is currently viewing a different one
                if (sessionHolder.getCurSessionId() != sessionId) {
                    sessionHolder.onLoadMessagesSuccess(sessionId)
                }

                // Retry to get response — sendUserQuery will set isTextStreaming = true
                // via startStreamingGen, so do NOT set it to false here.
                sendUserQuery("", provider, true)
            } catch (e: Exception) {
                Log.e(TAG, "retryAssistantResponse# error: ", e)
                // Ensure generation state is cleaned up on failure
                sessionHolder.updateIsTextStreaming(sessionId, false)
                generationJobs.remove(sessionId)
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
                if (isLoadingFirst) repeat(6) { awaitFrame() }
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
                if (isLoadingFirst) sessionHolder.updateIsLoadingFirst(false)
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