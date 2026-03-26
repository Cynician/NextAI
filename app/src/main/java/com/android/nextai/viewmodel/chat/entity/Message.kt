package com.android.nextai.viewmodel.chat.entity

import androidx.compose.runtime.Immutable
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils.parseMarkdown
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Message(
    val msgId: Long,
    val role: Role,
    val content: String
) {
    val markdownElements: List<MarkdownElement> by lazy {
        parseMarkdown(content)
    }
}

val EmptyMessage = Message(
    msgId = System.currentTimeMillis(),
    role = Role.None,
    content = ""
)