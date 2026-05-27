package com.android.nextai.ui.screen.home.body.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.utils.IncrementalMarkdownParser

@Composable
internal fun AssistantBubbleView(
    messageId: Long,
    content: String,
    isStreaming: Boolean = false,
) {

    /**
     * IMPORTANT:
     * remember(content) !!!
     *
     * Different message -> different parser
     * Same message streaming -> same parser
     */
    val parser = remember(messageId) {
        IncrementalMarkdownParser().apply {
            if (!isStreaming) {
                update(content)
                complete()
            }
        }
    }

    LaunchedEffect(content) {
        if(isStreaming) parser.update(content)
    }

    /**
     * Flush pending buffer after stream finished
     *
     * Ensure the last markdown block becomes stable.
     */
    LaunchedEffect(isStreaming) {
        if (!isStreaming) {
            parser.complete()
        }
    }

    SelectionContainer {
        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Stable nodes
            parser.stableNodes.forEach { node ->
                AssistantBlockItem(node)
            }
            // Streaming node
            parser.pendingNodes.forEach { node ->
                AssistantBlockItem(node)
            }
        }
    }
}

@Composable
internal fun AssistantBlockItem(node: MarkdownNode) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
    ) {
        MarkdownNodeView(node)
    }
}