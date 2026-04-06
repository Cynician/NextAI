package com.android.nextai.viewmodel.chat.entity

import androidx.compose.runtime.Immutable
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Message(
    var msgId: String,
    var role: Role,
    val content: String = "",
    var markdown : MarkdownNode
)