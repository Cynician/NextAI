package com.android.nextai.domain.model.chat

object MessageType {
    const val NONE = 0
    const val USER = 1
    const val ASSISTANT = 2
    const val ERROR = 3
}

data class Message(
    val id: Long,
    val sessionId: Long,
    val providerName: String,
    val modelId: String, // id = name
    val type: Int,
    val reasoningContent: String,
    val content: String,
    val status: Int,
    val tokenCount: Int,
    val extra: String,
    val createdAt: Long,
    val updatedAt: Long,
)