package com.android.nextai.viewmodel.chat.entity

import androidx.compose.runtime.Immutable
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Message(
    var msgId: Long,
    var role: Role,
    val content: String = "",
    var blocks : List<MarkdownNode>
)

fun getUserMessage(uId: Long, query: String) : Message{
    return Message(
        msgId = uId,
        role = Role.User,
        blocks = listOf(MarkdownNode.Text(query))
    )
}

fun getAssistantMessage(aId: Long,blocks: List<MarkdownNode>) : Message{
    return Message(
        msgId = aId,
        role = Role.Assistant,
        blocks = blocks
    )
}