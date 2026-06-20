package com.android.nextai.domain.model.chat


data class ModelParams(
    val id: String = "0",
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 2048,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f,
    val extra: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)