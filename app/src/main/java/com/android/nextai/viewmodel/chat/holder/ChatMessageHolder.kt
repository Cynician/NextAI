package com.android.nextai.viewmodel.chat.holder

import com.android.nextai.data.datebase.room.entity.MessageEntity
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ViewModelScoped
class ChatMessageHolder @Inject constructor() {
    /**
     * Generate state
     * Record the current generation status of assistant, including text, graphics, and voice
     */
    private val _isTextStreaming = MutableStateFlow(false)
    private val _isImageGenerating = MutableStateFlow(false)

    val isGenerating: Flow<Boolean> = combine(
        _isTextStreaming, _isImageGenerating
    ) { text, image ->
        text || image
    }

    fun updateIsTextStreaming(state: Boolean) {
        _isTextStreaming.value = state
    }

    /**
     * Record the current user's query
     */
    private val _curQuery = MutableStateFlow("")

    fun updateCurQuery(query: String) {
        _curQuery.value = query
    }


    /**
     * Record current assistant's response
     */
    private val _curResponse = MutableStateFlow("")

    val curResponse: StateFlow<String> = _curResponse.asStateFlow()

    fun clearCurrentResponse() {
        _curResponse.value = ""
    }

    fun updateCurResponse(content: String) {
        _curResponse.value += content
    }

    fun getCurResponse(): String {
        return curResponse.value
    }

    /**
     * Message list scrolling logic
     */

    private val _scrollToLatestMessageEvent = MutableSharedFlow<Unit>()

    val scrollToLatestMessageEvent = _scrollToLatestMessageEvent.asSharedFlow()

    suspend fun emitScrollToLatestMessageEvent() {
        _scrollToLatestMessageEvent.emit(Unit)
    }

    /**
     * Record messages info in a session
     */
    private val _messageList = MutableStateFlow<List<MessageEntity>>(emptyList())

    val messageList = _messageList.asStateFlow()

    fun setMessages(
        messages: List<MessageEntity>
    ) {
        _messageList.value = messages
    }

    fun getMessages():List<MessageEntity>{
        return _messageList.value
    }

    fun addMessage(newM: MessageEntity) {
        _messageList.update {
            it + newM
        }
    }

    fun updateLastMessage(newM: MessageEntity) {
        _messageList.update { list ->
            if (list.isEmpty()) {
                list
            } else {
                list.toMutableList().apply {
                    this[lastIndex] = newM
                }
            }
        }
    }

    fun addMessagesToHead(
        newMessages: List<MessageEntity>
    ) {
        _messageList.update {
            newMessages + it
        }
    }

    /**
     * Init for creating/switching a new session
     */
    fun initSession() {
        _isTextStreaming.value = false
        _messageList.value = emptyList()
        _curQuery.value = ""
        _curResponse.value = ""
        _curMessagesMinId.value = Long.MAX_VALUE
    }

    /**
     * Load messages
     *
     */
    private val _isLoadingFirst = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _hasMoreMessages = MutableStateFlow(true)
    private val _curMessagesMinId = MutableStateFlow(Long.MAX_VALUE)

    val isLoadingFirst = _isLoadingFirst.asStateFlow()
    val isLoadingMore: Boolean get() = _isLoadingMore.value
    val hasMoreMessages:Boolean get() = _hasMoreMessages.value
    val curMessagesMinId: Long get() = _curMessagesMinId.value

    fun getIsLoadingFirst(): Boolean{
        return _isLoadingFirst.value
    }

    fun updateHasMoreMessages(
        value: Boolean
    ) {
        _hasMoreMessages.value = value
    }

    fun updateIsFirstLoadMessages(state: Boolean){
        _isLoadingFirst.value = state
    }

    fun updateIsLoadingMore(state: Boolean){
        _isLoadingMore.value = state
    }

    fun updateCurMessagesMinId(id: Long){
        _curMessagesMinId.value = id
    }

    fun loadMessagesInit() {
        _isTextStreaming.value = false
        _curQuery.value = ""
        _curResponse.value = ""
        _curMessagesMinId.value = Long.MAX_VALUE
        _isLoadingFirst.value = true
        _isLoadingMore.value = false
        _hasMoreMessages.value = true
    }
}