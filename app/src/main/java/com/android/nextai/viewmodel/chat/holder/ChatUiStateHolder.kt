package com.android.nextai.viewmodel.chat.holder

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ViewModelScoped
class ChatUiStateHolder @Inject constructor(
    @ApplicationContext context: Context,
) {
    // record weather the streaming is generating or not, which is use to scroll logic
    private val _streamingTick = MutableStateFlow(0)
    val streamingTick = _streamingTick.asStateFlow()
    fun updateStreamingTick(tick: Int) {
        _streamingTick.value += tick
    }

    // record is time to the latest assistant message
    private val _scrollToHeadAssistMessage = MutableStateFlow(false)
    val scrollToHeadAssistMessage = _scrollToHeadAssistMessage.asStateFlow()
    fun updateScrollToHeadAssistMessage(state: Boolean) {
        _scrollToHeadAssistMessage.value = state
    }
}