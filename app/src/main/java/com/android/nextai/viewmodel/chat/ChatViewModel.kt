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
import com.android.nextai.viewmodel.chat.holder.ChatUiStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val messageHolder: ChatMessageHolder,
    val uiStateHolder: ChatUiStateHolder,
    val sessionHolder: ChatSessionHolder,
    val chatRemoteRepository: ChatRemoteRepository,
    val chatDatabaseRepository: ChatDatabaseRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "ChatViewModel"
    }

    var generationJob: Job? = null

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
        generationJob = viewModelScope.launch {
            try {
                messageHolder.updateCurQuery(query)
                messageHolder.clearCurrentResponse()
                Log.d(TAG, "create session with query :$query")
                var userMessage: MessageEntity
                if (!sessionHolder.getIsInSession()) {
                    val (_, tmpMessage) = chatDatabaseRepository.createSessionWithUserMessage(query)
                    userMessage = tmpMessage
                    sessionHolder.updateIsInSession(true)
                    sessionHolder.updateCurSessionId(tmpMessage.sessionId)
                    sessionHolder.loadSessions()
                } else {
                    userMessage = chatDatabaseRepository.createMessage(
                        sessionId = sessionHolder.getCurSessionId(),
                        content = query,
                        role = Role.User
                    )
                }
                messageHolder.addMessage(userMessage)
                if (isSportStreamingGen) {
                    messageHolder.updateIsTextStreaming(true)
                    uiStateHolder.updateScrollToHeadAssistMessage(true)
                    startStreamingGen()
                }
            } catch (e: Exception) {
                Log.e(TAG, "error: $e")
                generationJob?.cancel()
            }
        }
    }

    /**
     * Get ai response in stream way
     */
    private suspend fun startStreamingGen() {
        Log.d(TAG, "startStreamingGen# get ai response")
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
                        uiStateHolder.updateStreamingTick(event.content.length)
                        messageHolder.updateLastMessage(
                            assistantMessage.copy(content = messageHolder.getCurResponse())
                        )
                    }
                    is GenerationEvent.Done -> {
                        chatDatabaseRepository.messageDao.updateMessageContent(
                            id = assistantMessage.id,
                            content = messageHolder.getCurResponse()
                        )
                        messageHolder.updateIsTextStreaming(false)
                        uiStateHolder.updateScrollToHeadAssistMessage(false)
                        generationJob?.cancel()
                    }
                    is GenerationEvent.Error -> {}
                }
            }
    }


    /**
     * Session init
     *
     * The initialization method when creating a new session or selecting a different session
     */
    fun initSession() {
        messageHolder.init()
        sessionHolder.init()
        generationJob?.cancel()
    }

    /**
     * Delete sessions in batch
     */
    fun batchDeleteSessions(idList: List<Long>) {
        viewModelScope.launch {
            if (idList.contains(sessionHolder.getCurSessionId())) {
                initSession()
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
}