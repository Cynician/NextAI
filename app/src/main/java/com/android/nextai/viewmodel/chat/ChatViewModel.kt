package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.sqlite.entity.MessageEntity
import com.android.nextai.domain.remote.ApiType
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatDatabaseRepository
import com.android.nextai.domain.repository.ChatRemoteRepository
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.holder.ChatMessageHolder
import com.android.nextai.viewmodel.chat.holder.ChatSessionHolder
import com.android.nextai.viewmodel.chat.holder.MarkdownCacheHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val messageHolder: ChatMessageHolder,
    val sessionHolder: ChatSessionHolder,
    val markdownCacheHolder: MarkdownCacheHolder,
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
    fun sendUserQuery(
        query: String,
        provider: ProviderEntity
    ) {
        val isSportStreamingGen = true
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
                    startStreamingGen(provider)
                }
            } catch (e: Exception) {
                Log.e(TAG, "error: ", e)
                generationJob?.cancel()
            }
        }
    }

    // Get ai response in stream way
    private suspend fun startStreamingGen(
        provider: ProviderEntity
    ) {
        Log.d(TAG, "startStreamingGen# get ai response")
        messageHolder.updateIsTextStreaming(true)
        val assistantMessage = chatDatabaseRepository.createMessage(
            sessionId = sessionHolder.getCurSessionId(),
            content = messageHolder.getCurResponse(),
            role = Role.Assistant
        )
        val parser = markdownCacheHolder.getOrCreate(assistantMessage.id)
        messageHolder.addMessage(assistantMessage)
        messageHolder.emitScrollToLatestMessageEvent()
        chatRemoteRepository.streamingGen(
            apiType = ApiType.OPENAI,
            messageList = messageHolder.getMessages(),
            provider,
        )
            .collect { event ->
                when (event) {
                    is GenerationEvent.Word -> {
                        messageHolder.updateCurResponse(content = event.content)
                        val content = messageHolder.getCurResponse()
                        parser.appendDelta(event.content)
                        messageHolder.updateLastMessage(assistantMessage.copy(content = content))
                    }

                    is GenerationEvent.Done -> {
                        parser.complete()
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
     */
    fun initSession() {
        messageHolder.initSession()
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
            sessionHolder.batchDeleteSessions(idList)
        }
    }

    fun batchPinSessions(idList: List<Long>) {
        viewModelScope.launch {
            sessionHolder.batchPinSessions(idList)
        }
    }

    fun batchUnpinSessions(idList: List<Long>){
        viewModelScope.launch {
            sessionHolder.batchUnpinSessions(idList)
        }
    }

    /**
     * Load messages
     */
    fun loadMessages(
        sessionId: Long,
        callBack: (() -> Unit)? = null,
        isLoadingFirst: Boolean
    ) {
        loadMessagesJob?.cancel()
        loadMessagesJob = viewModelScope.launch {
            try {
                val messageList = chatDatabaseRepository.getPageBefore(
                    sessionId = sessionId,
                    minMsgId = messageHolder.curMessagesMinId
                ).reversed()
                if (messageList.isNotEmpty()) {
                     // Parse only once
                    messageList.forEach { message ->
                        if (message.role == Role.Assistant.name) {
                            val cached = markdownCacheHolder.get(message.id)
                            // Already parsed
                            if (cached == null) {
                                val parser = markdownCacheHolder.getOrCreate(message.id)
                                parser.setContent(message.content)
                            }
                        }
                    }
                    messageHolder.updateCurMessagesMinId(id = messageList.first().id)
                    if(isLoadingFirst){
                        messageHolder.setMessages(messageList)
                    }else{
                        messageHolder.addMessagesToHead(messageList)
                    }
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

    suspend fun loadFirstPageMessages(sessionId: Long) {

        sessionHolder.loadMessagesInit(sessionId)
        messageHolder.loadMessagesInit()

        // Wait for loading animation finish
        awaitFrame()

        loadMessages(
            sessionId = sessionId,
            callBack = {
                viewModelScope.launch {
                    messageHolder.updateIsFirstLoadMessages(false)
                    messageHolder.emitScrollToLatestMessageEvent()
                }
            },
            isLoadingFirst = true
        )
    }

    fun loadNextPageMessages() {
        if (
            !messageHolder.hasMoreMessages ||
            messageHolder.isLoadingMore ||
            messageHolder.getIsLoadingFirst() ||
            !sessionHolder.isInSession
        ) {
            Log.d(TAG, "loadNextPageMessages# skip to load messages")
            return
        }

        Log.d(TAG, "loadNextPageMessages# load next page")
        messageHolder.updateIsLoadingMore(true)
        loadMessages(
            sessionId = sessionHolder.getCurSessionId(),
            callBack = {
                messageHolder.updateIsLoadingMore(false)
                Log.d(TAG, "loadNextPageMessages# success")
            },
            isLoadingFirst = false
        )
    }
}