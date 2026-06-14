package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.data.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datebase.room.entity.MessageEntity
import com.android.nextai.data.remote.ApiType
import com.android.nextai.domain.model.GenerationEvent
import com.android.nextai.repository.ChatDatabaseRepository
import com.android.nextai.repository.ChatRemoteRepository
import com.android.nextai.domain.model.Role
import com.android.nextai.viewmodel.chat.holder.ChatSessionHolder
import com.android.nextai.viewmodel.chat.holder.MarkdownCacheHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val sessionHolder: ChatSessionHolder,
    val markdownCacheHolder: MarkdownCacheHolder,
    val chatRemoteRepository: ChatRemoteRepository,
    val chatDatabaseRepository: ChatDatabaseRepository,
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

    /**
     * Get ai response
     */
    fun sendUserQuery(
        query: String,
        provider: ProviderEntity,
    ) {
        val isSportStreamingGen = true
        val isInSession = sessionHolder.isInSession
        val activeSessionId = sessionHolder.getCurSessionId()

        viewModelScope.launch {
            try {
                var targetSessionId = activeSessionId
                var userMessage: MessageEntity

                // 1. Preprocessing: If not in a Session, it means a new session needs to be created.
                if (!isInSession) {
                    Log.d(TAG, "create session with query :$query")
                    val (_, tmpMessage) = chatDatabaseRepository.createSessionWithUserMessage(query)
                    userMessage = tmpMessage.copy()
                    targetSessionId = userMessage.sessionId
                    // Set the new session to the current global active state.
                    sessionHolder.updateIsInSession(true)
                    sessionHolder.updateCurSessionId(targetSessionId)
                    sessionHolder.updateCurMessagesMinId(targetSessionId, userMessage.id)
                } else {
                    // 2. Already in the session, append the message under this targetSessionId.
                    userMessage = chatDatabaseRepository.createMessage(
                        sessionId = targetSessionId, content = query, role = Role.User
                    )
                }

                generationJobs[targetSessionId]?.cancel()

                // 3. Starts a separate backend job pipeline for this session.
                generationJobs[targetSessionId] = viewModelScope.launch {
                    sessionHolder.clearCurrentResponse(targetSessionId)
                    sessionHolder.loadingSessions()
                    sessionHolder.addMessage(targetSessionId, userMessage)

                    // Only when the target session matches the global selected session.
                    if (targetSessionId == sessionHolder.getCurSessionId()) {
                        sessionHolder.emitScrollToLatestMessageEvent(targetSessionId)
                    }

                    // 4. Start streaming generation in the background.
                    if (isSportStreamingGen) {
                        startStreamingGen(targetSessionId, provider)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "error: ", e)
            }
        }
    }

    /**
     * Backend stream generation core function.
     */
    private suspend fun startStreamingGen(
        sessionId: Long,
        provider: ProviderEntity,
    ) {
        Log.d(TAG, "startStreamingGen# session: $sessionId")
        sessionHolder.updateIsTextStreaming(sessionId, true)

        val assistantMessage = chatDatabaseRepository.createMessage(
            sessionId = sessionId,
            content = sessionHolder.getState(sessionId).curResponse,
            role = Role.Assistant
        )
        val parser = markdownCacheHolder.getOrCreate(assistantMessage.id)
        sessionHolder.addMessage(sessionId, assistantMessage)

        if (sessionId == sessionHolder.getCurSessionId()) {
            sessionHolder.emitScrollToLatestMessageEvent(sessionId)
        }

        // Passing the full context from the session's dedicated memory cache to the large model.
        chatRemoteRepository.streamingGen(
            apiType = ApiType.OPENAI,
            messageList = sessionHolder.getState(sessionId).messageList,
            provider,
        ).collect { event ->
            when (event) {
                is GenerationEvent.Word -> {
                    sessionHolder.updateCurResponse(sessionId, event.content)
                    val content = sessionHolder.getState(sessionId).curResponse
                    parser.appendDelta(event.content)
                    // Refresh the last message in the dedicated memory map of the session in real
                    // time. If the user switches back to the session, the UI will see text ticking
                    // in real time.
                    sessionHolder.updateLastestMessage(
                        sessionId, assistantMessage.copy(content = content)
                    )
                }

                is GenerationEvent.Done -> {
                    parser.complete()
                    chatDatabaseRepository.updateMessageContent(
                        sessionId = sessionId,
                        msgId = assistantMessage.id,
                        content = sessionHolder.getState(sessionId).curResponse
                    )
                    sessionHolder.updateIsTextStreaming(sessionId, false)
                    sessionHolder.loadingSessions()
                    generationJobs.remove(sessionId) // Task completes normally, removing the Job reference.
                }

                is GenerationEvent.Error -> {
                    sessionHolder.updateIsTextStreaming(sessionId, false)
                    generationJobs.remove(sessionId)
                }
            }
        }
    }

    /**
     * Session init
     */
    fun initSession() {
        sessionHolder.initSession()
    }

    /**
     * Batch operation
     */
    fun batchDeleteSessions(idList: List<Long>) {
        viewModelScope.launch {
            if (idList.contains(sessionHolder.getCurSessionId())) {
                initSession()
            }
            sessionHolder.onBatchDeleteSessions(idList)
            idList.forEach { id ->
                sessionHolder.removeSession(id)

                // 3. Should cut off their streaming or load messages jobs that might still
                // be looping in the background.
                generationJobs[id]?.cancel()
                generationJobs.remove(id)
                loadMessagesJobs[id]?.cancel()
                loadMessagesJobs.remove(id)
            }
        }
    }

    fun batchPinSessions(idList: List<Long>) {
        viewModelScope.launch {
            sessionHolder.onBatchPinSessions(idList)
        }
    }

    fun batchUnpinSessions(idList: List<Long>) {
        viewModelScope.launch {
            sessionHolder.onBatchUnpinSessions(idList)
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
                val sessionStates = sessionHolder.getState(sessionId)
                val messageList = chatDatabaseRepository.getPageBefore(
                    sessionId = sessionId, minMsgId = sessionStates.curMessagesMinId
                ).reversed()

                if (messageList.isNotEmpty()) {
                    yield()
                    messageList.forEach { message ->
                        if (message.role == Role.Assistant.name) {
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
                sessionHolder.updateIsLoadingFirst(false)
                loadMessagesJobs.remove(sessionId)
            }
        }
    }

    /**
     * Handles the logic when a user clicks a sidebar session, updates status,
     * and loading paginated messages。
     */
    fun onChangingSession(sessionId: Long) {
        val cachedMessages = sessionHolder.getState(sessionId).messageList
        sessionHolder.updateIsLoadingFirst(true)
        if (cachedMessages.isNotEmpty()) {
            sessionHolder.onLoadMessagesSuccess(sessionId)
        } else {
            loadMessages(
                sessionId = sessionId,
                isLoadingFirst = true,
                callBack = {
                    sessionHolder.onLoadMessagesSuccess(sessionId)
                }
            )
        }
        sessionHolder.updateIsLoadingFirst(false)
    }

    /**
     * Swipe up to the top to load the next page.
     */
    fun loadNextPageMessages() {
        val currentSessionId = sessionHolder.getCurSessionId()
        val sessionState = sessionHolder.getState(currentSessionId)

        if (sessionState.canLoadingMore || !sessionHolder.isInSession) {
            Log.d(TAG, "loadNextPageMessages# skip to load messages")
            return
        }

        Log.d(TAG, "loadNextPageMessages# load next page for $currentSessionId")
        loadMessages(
            sessionId = currentSessionId,
            isLoadingFirst = false,
            callBack = {
                Log.d(TAG, "loadNextPageMessages# success")
            }
        )
    }
}