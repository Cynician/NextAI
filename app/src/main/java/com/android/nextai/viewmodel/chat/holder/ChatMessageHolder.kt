package com.android.nextai.viewmodel.chat.holder

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.nextai.domain.database.db.entity.MessageEntity
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _scrollToLatestMessageEvent = MutableStateFlow(0)

    val scrollToLatestMessageEvent = _scrollToLatestMessageEvent.asStateFlow()

    fun emitScrollToLatestMessageEvent() {
        _scrollToLatestMessageEvent.update { it + 1 }
    }

    /**
     * Record messages info in a session
     */
    private val _messageList = mutableStateListOf<MessageEntity>()

    val messageList: SnapshotStateList<MessageEntity> = _messageList

    fun addMessage(newM: MessageEntity) {
        _messageList.add(newM)
    }

    fun updateLastMessage(newM: MessageEntity) {
        _messageList[_messageList.lastIndex] = newM
    }

    fun addMessagesToHead(
        newMessages: List<MessageEntity>
    ) {
        _messageList.addAll(index = 0, elements = newMessages)
    }

    /**
     * Init for creating a new session
     */
    fun createSessionInit() {
        _isTextStreaming.value = false
        _messageList.clear()
        _curQuery.value = ""
        _curResponse.value = ""
        _curMessagesMinId.value = Long.MAX_VALUE
    }

    /**
     * Load messages
     *
     */
    private val _isFirstLoadMessages = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _hasMoreMessages = MutableStateFlow(true)
    private val _curMessagesMinId = MutableStateFlow(Long.MAX_VALUE)


    val isFirstLoadMessages: Boolean get() = _isFirstLoadMessages.value
    val isLoadingMore: Boolean get() = _isLoadingMore.value
    val hasMoreMessages:Boolean get() = _hasMoreMessages.value
    val curMessagesMinId: Long get() = _curMessagesMinId.value

    fun updateHasMoreMessages(
        value: Boolean
    ) {
        _hasMoreMessages.value = value
    }

    fun updateIsFirstLoadMessages(state: Boolean){
        _isFirstLoadMessages.value = state
    }

    fun updateIsLoadingMore(state: Boolean){
        _isLoadingMore.value = state
    }

    fun updateCurMessagesMinId(id: Long){
        _curMessagesMinId.value = id
    }

    fun loadMessagesInit() {
        _isTextStreaming.value = false
        _isImageGenerating.value = false
        _curQuery.value = ""
        _curResponse.value = ""
        _messageList.clear()
        _curMessagesMinId.value = Long.MAX_VALUE
        _isFirstLoadMessages.value = true
        _isLoadingMore.value = false
        _hasMoreMessages.value = true
    }
}