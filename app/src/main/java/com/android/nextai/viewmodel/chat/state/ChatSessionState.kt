package com.android.nextai.viewmodel.chat.state

import com.android.nextai.data.datebase.room.entity.MessageEntity

/**
 * States of a single Session
 */
data class ChatSessionState(
    val sessionId: Long,
    val messageList: List<MessageEntity> = emptyList(),
    val isTextGenerating: Boolean = false,
    val isImageGenerating: Boolean = false,
    val curResponse: String = "",
    val curMessagesMinId: Long = Long.MAX_VALUE,
    val hasMoreMessages: Boolean = true,

) {
    // Derivative States
    val isGenerating: Boolean get() = isTextGenerating || isImageGenerating
    val canLoadingMore: Boolean get() = !hasMoreMessages
}