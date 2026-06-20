package com.android.nextai.domain.model.chat

object GroupType {
    const val PIN = 0
    const val TODAY = 1
    const val IN_WEEK = 2
    const val IN_MONTH = 3
    const val EARLIER = 4
}

data class Session(
    val id: Long,
    val providerId: String,
    val modelId: String,
    val modelParams: ModelParams,
    val title: String,
    val aiTitle: String = "",
    val systemPrompt: String = "解答用户问题",
    val tokenCount: Int = 0,
    val isPinned: Int = 0,
    val isDeleted: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)