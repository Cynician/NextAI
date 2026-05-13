package com.android.nextai.viewmodel.chat.holder

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ViewModelScoped
class ChatUiStateHolder @Inject constructor() {

    /**
     * Message list scrolling logic
     */
    private val _streamingTick = MutableStateFlow(0)
    private val _scrollToHeadAssistMessage = MutableStateFlow(false)

    val streamingTick = _streamingTick.asStateFlow()
    val scrollToHeadAssistMessage = _scrollToHeadAssistMessage.asStateFlow()

    fun updateStreamingTick(tick: Int) {
        _streamingTick.value += tick
    }

    fun updateScrollToHeadAssistMessage(state: Boolean) {
        _scrollToHeadAssistMessage.value = state
    }
}