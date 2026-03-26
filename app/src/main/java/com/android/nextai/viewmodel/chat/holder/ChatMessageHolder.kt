package com.android.nextai.viewmodel.chat.holder

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.viewmodel.chat.entity.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ViewModelScoped
class ChatMessageHolder @Inject constructor(
    @ApplicationContext context: Context,
    private val chatRepository: ChatRepository
) {

    private var _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating
    fun updateIsGenerating(state: Boolean){
        _isGenerating.value = state
    }


    private val _curPrompt = MutableStateFlow("")
    val curPrompt: StateFlow<String> = _curPrompt.asStateFlow()
    fun updateCurPrompt(prompt:String){
        _curPrompt.value = prompt
    }


    private val _messageList = mutableStateListOf<Message>()
    val messageList: SnapshotStateList<Message> = _messageList

    private val _totalMarkdownElementCnt = MutableStateFlow(0)
    val totalMarkdownElementCnt: MutableStateFlow<Int> = _totalMarkdownElementCnt
    fun updateTotalMarkdownElementCnt(num:Int){
        _totalMarkdownElementCnt.value += num
    }

    private val _lastUserMarkdownElementCnt = MutableStateFlow(0)
    val lastUserMarkdownElementCnt: MutableStateFlow<Int> = _lastUserMarkdownElementCnt
    fun updateLastUserMarkdownElementCnt(num:Int){
        _lastUserMarkdownElementCnt.value = num
    }


}