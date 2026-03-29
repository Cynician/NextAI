package com.android.nextai.viewmodel.chat.entity

import androidx.compose.runtime.Immutable
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils.parseMarkdown
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Message(
    val msgId: Int,
    val role: Role,
    val content: String = "",
    var markdown : MarkdownElement
)

val EmptyMessage = Message(
    msgId = 0,
    role = Role.None,
    content = "",
    markdown = MarkdownElement.Body("")
)