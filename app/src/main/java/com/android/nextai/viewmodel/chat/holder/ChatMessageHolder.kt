package com.android.nextai.viewmodel.chat.holder

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.android.nextai.viewmodel.chat.entity.Message
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ViewModelScoped
class ChatMessageHolder @Inject constructor() : ViewModel() {
    /**
     * Generate state
     * Record the current generation status of assistant, including text, graphics, and voice
     */
    private var _isGenerating = MutableStateFlow(false)
    private val _isTextStreaming = MutableStateFlow(true)

    val isGenerating: StateFlow<Boolean> = _isGenerating
    val isTextStreaming: StateFlow<Boolean> = _isTextStreaming.asStateFlow()

    fun updateIsGenerating(state: Boolean) {
        _isGenerating.value = state
    }

    fun updateIsTextStreaming(state: Boolean) {
        _isTextStreaming.value = state
    }

    /**
     * Record the current user's query
     */
    private val _curQuery = MutableStateFlow("")

    val curQuery: StateFlow<String> = _curQuery.asStateFlow()

    fun updateCurQuery(query: String) {
        _curQuery.value = query
    }

    fun getCurQuery(): String {
        return curQuery.value
    }

    /**
     * Record current assistant's response
     */
    private val _curResponse = MutableStateFlow("")

    val curResponse: StateFlow<String> = _curResponse.asStateFlow()

    fun updateCurResponse(content: String) {
        _curResponse.value += content
    }

    fun getCurResponse(): String {
        return curResponse.value
    }

    /**
     * Record messages info in a session
     */
    private val _messageList = mutableStateListOf<Message>()

    val messageList: SnapshotStateList<Message> = _messageList

    fun addMessage(newM: Message) {
        _messageList.add(newM)
    }

    fun updateLastMessage(newM: Message) {
        _messageList[_messageList.lastIndex] = newM
    }

    /**
     * The initialization method when creating a new session or when selecting a different session
     */
    fun init() {
        _isGenerating.value = false
        _isTextStreaming.value = false
        _messageList.clear()
        _curQuery.value = ""
        _curResponse.value = ""
    }
}