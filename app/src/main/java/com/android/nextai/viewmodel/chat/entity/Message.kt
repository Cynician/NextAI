package com.android.nextai.viewmodel.chat.entity

import androidx.compose.runtime.Immutable
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Immutable
data class Message(
    var msgId: String,
    var role: Role,
    val content: String = "",
    var blocks : List<MarkdownNode>
)

fun getUserMessage(query: String) : Message{
    return Message(
        msgId = UUID.randomUUID().toString(),
        role = Role.User,
        blocks = listOf(MarkdownNode.Text(query))
    )
}

fun getAssistantMessage(blocks: List<MarkdownNode>) : Message{
    return Message(
        msgId = UUID.randomUUID().toString(),
        role = Role.Assistant,
        blocks = blocks
    )
}