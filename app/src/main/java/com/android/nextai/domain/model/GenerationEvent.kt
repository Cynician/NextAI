package com.android.nextai.domain.model

sealed class GenerationEvent{
    data class Word(val content:String):GenerationEvent()
    data object Done : GenerationEvent()
    data class Error(val content: String) : GenerationEvent()
}