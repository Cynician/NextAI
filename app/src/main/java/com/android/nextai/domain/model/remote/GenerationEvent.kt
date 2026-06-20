package com.android.nextai.domain.model.remote

sealed class GenerationEvent {
    data class Chunk(val content: String) : GenerationEvent()
    data object Done : GenerationEvent()
    data class Error(val content: String) : GenerationEvent()
}