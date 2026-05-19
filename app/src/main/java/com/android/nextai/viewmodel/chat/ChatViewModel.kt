package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.database.db.entity.MessageEntity
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatDatabaseRepository
import com.android.nextai.domain.repository.ChatRemoteRepository
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.holder.ChatMessageHolder
import com.android.nextai.viewmodel.chat.holder.ChatSessionHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val messageHolder: ChatMessageHolder,
    val sessionHolder: ChatSessionHolder,
    val chatRemoteRepository: ChatRemoteRepository,
    val chatDatabaseRepository: ChatDatabaseRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "ChatViewModel"
    }

    var generationJob: Job? = null
    var loadMessagesJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                sessionHolder.loadSessions()
            } catch (e: Exception) {
                Log.e(TAG, "Could not load sessions: $e")
            }
        }
    }

    /**
     * Get ai response
     */
    fun sendUserQuery(query: String) {
        val isSportStreamingGen = true
        val query = query.trim()
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            try {
                // Pre-process
                messageHolder.updateCurQuery(query)
                messageHolder.clearCurrentResponse()
                var userMessage: MessageEntity
                // Create a new session
                if (!sessionHolder.isInSession) {
                    Log.d(TAG, "create session with query :$query")
                    val (_, tmpMessage) = chatDatabaseRepository.createSessionWithUserMessage(query)
                    userMessage = tmpMessage.copy()
                    sessionHolder.updateIsInSession(true)
                    sessionHolder.updateCurSessionId(userMessage.sessionId)
                    messageHolder.updateCurMessagesMinId(userMessage.id)
                } else {
                    userMessage = chatDatabaseRepository.createMessage(
                        sessionId = sessionHolder.getCurSessionId(),
                        content = query,
                        role = Role.User
                    )
                }
                sessionHolder.loadSessions()
                messageHolder.addMessage(userMessage)
                messageHolder.emitScrollToLatestMessageEvent()
                // Core process
                if (isSportStreamingGen) {
                    startStreamingGen()
                }
            } catch (e: Exception) {
                Log.e(TAG, "error: $e")
                generationJob?.cancel()
            }
        }
    }

    // Get ai response in stream way
    private suspend fun startStreamingGen() {
        Log.d(TAG, "startStreamingGen# get ai response")
        messageHolder.updateIsTextStreaming(true)
        val assistantMessage = chatDatabaseRepository.createMessage(
            sessionId = sessionHolder.getCurSessionId(),
            content = messageHolder.getCurResponse(),
            role = Role.Assistant
        )
        messageHolder.addMessage(assistantMessage)
        chatRemoteRepository.streamingGen(
            model = Model.TEST,
            messageList = messageHolder.messageList
        )
            .collect { event ->
                when (event) {
                    is GenerationEvent.Word -> {
                        messageHolder.updateCurResponse(content = event.content)
                        messageHolder.updateLastMessage(
                            assistantMessage.copy(content = messageHolder.getCurResponse())
                        )
                    }

                    is GenerationEvent.Done -> {
                        chatDatabaseRepository.updateMessageContent(
                            sessionId = sessionHolder.getCurSessionId(),
                            msgId = assistantMessage.id,
                            content = messageHolder.getCurResponse()
                        )
                        messageHolder.updateIsTextStreaming(false)
                        sessionHolder.loadSessions()
                    }

                    is GenerationEvent.Error -> {
                        messageHolder.updateIsTextStreaming(false)
                    }
                }
            }
    }


    /**
     * Session init
     *
     * The initialization method when creating a new session or selecting a different session
     */
    fun createSessionInit() {
        messageHolder.createSessionInit()
        sessionHolder.createSessionInit()
    }

    /**
     * Delete sessions in batch
     */
    fun batchDeleteSessions(idList: List<Long>) {
        viewModelScope.launch {
            if (idList.contains(sessionHolder.getCurSessionId())) {
                createSessionInit()
            }
            sessionHolder.batchDeleteSessions(idList)
        }
    }

    /**
     * Pin sessions in batch
     */
    fun batchPinSessions(idList: List<Long>) {
        viewModelScope.launch {
            sessionHolder.batchPinSessions(idList)
        }
    }

    /**
     * Unpinned session
     */
    fun unpinnedSession(id: Long) {
        viewModelScope.launch {
            sessionHolder.unpinnedSession(id)
        }
    }

    /**
     * Load messages
     */
    fun loadMessages(sessionId: Long, callBack: (() -> Unit)? = null) {
        loadMessagesJob?.cancel()
        loadMessagesJob = viewModelScope.launch {
            try {
                val messageList = chatDatabaseRepository.messageDao.getMessagesBefore(
                    sessionId = sessionId,
                    id = messageHolder.curMessagesMinId
                ).reversed()
                if (messageList.isNotEmpty()) {
                    messageHolder.updateCurMessagesMinId(id = messageList.first().id)
                    messageHolder.addMessagesToHead(messageList)
                    Log.d(TAG, "loadMessages# size:${messageList.size}")
                } else {
                    messageHolder.updateHasMoreMessages(false)
                }
                if (callBack != null) callBack()
            } finally {
                messageHolder.updateIsFirstLoadMessages(false)
                messageHolder.updateIsLoadingMore(false)
            }

        }
    }

    fun loadMessagesInit(sessionId: Long) {
        sessionHolder.loadMessagesInit(sessionId)
        messageHolder.loadMessagesInit()
        loadMessages(
            sessionId = sessionId,
            callBack = {
                messageHolder.updateIsFirstLoadMessages(false)
                messageHolder.emitScrollToLatestMessageEvent()
            }
        )
    }

    fun loadMoreMessages() {
        if (
            !messageHolder.hasMoreMessages ||
            messageHolder.isLoadingMore ||
            messageHolder.isFirstLoadMessages ||
            !sessionHolder.isInSession
        ) {
            Log.d(TAG, "loadMoreMessages# skip to load messages")
            return
        }

        Log.d(TAG, "loadMoreMessages# load more messages")
        messageHolder.updateIsLoadingMore(true)
        loadMessages(
            sessionId = sessionHolder.getCurSessionId(),
            callBack = {
                messageHolder.updateIsLoadingMore(false)
                Log.d(TAG, "loadMoreMessages# success")
            }
        )
    }
}