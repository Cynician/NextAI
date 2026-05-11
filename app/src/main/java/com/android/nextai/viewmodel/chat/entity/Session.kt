package com.android.nextai.viewmodel.chat.entity

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Session(
    var id: Long,
    val title: String,
    val aiTitle: String,
    val createdAt: Long,
    val updatedAt: Long,
    val systemPrompt: String,
    val isPinned: Int,
    val tokenCount: Int,
)

enum class SessionGroup(val title:String){
    PINNED("置顶"), TODAY("今天"), IN_WEEK("最近7天"), IN_MONTH("最近30天"), EARLIER("更早")
}