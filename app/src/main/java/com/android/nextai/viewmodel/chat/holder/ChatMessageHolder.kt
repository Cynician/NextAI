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
import javax.inject.Inject

@ViewModelScoped
class ChatMessageHolder @Inject constructor() {
    /**
     * Generate state
     * Record the current generation status of assistant, including text, graphics, and voice
     */
    private val _isTextStreaming = MutableStateFlow(true)
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

    fun clearCurrentResponse() {
        _curResponse.value.clear()
    }

    /**
     * Record current assistant's response
     */
    private val _curResponse = MutableStateFlow(StringBuilder(""))

    val curResponse: StateFlow<StringBuilder> = _curResponse.asStateFlow()

    fun updateCurResponse(content: String) {
        _curResponse.value.append(content)
    }

    fun getCurResponse(): String {
        return curResponse.value.toString()
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

    /**
     * The initialization method when creating a new session or when selecting a different session
     */
    fun init() {
        _isTextStreaming.value = false
        _messageList.clear()
        _curQuery.value = ""
        _curResponse.value.clear()
    }
}