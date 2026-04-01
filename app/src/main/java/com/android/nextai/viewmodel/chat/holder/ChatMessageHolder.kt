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
    // to record the generating state, no whether it is text/image/voice generating
    private var _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating
    fun updateIsGenerating(state: Boolean){
        _isGenerating.value = state
    }

    // to record current user's prompt
    private val _curPrompt = MutableStateFlow("")
    val curPrompt: StateFlow<String> = _curPrompt.asStateFlow()
    fun updateCurPrompt(prompt:String){
        _curPrompt.value = prompt
    }

    // record messages in a session
    private val _messageList = mutableStateListOf<Message>()
    val messageList: SnapshotStateList<Message> = _messageList


    // record text streaming state
    private val _isTextStreaming = MutableStateFlow(true)
    val isTextStreaming: StateFlow<Boolean> = _isTextStreaming.asStateFlow()
    fun updateIsTextStreaming(state:Boolean){
        _isTextStreaming.value = state
    }

}