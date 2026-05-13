package com.android.nextai.ui.screen.home.body.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.utils.MarkdownNodeUtils.parseMarkDown

@Composable
internal fun AssistantStreamBubbleView(content: String) {

    val parser = remember { IncrementalMarkdownParser() }

    LaunchedEffect(content) {
        parser.update(content)
    }

    SelectionContainer {
        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Stable nodes
            parser.stableNodes.forEach { node ->
                key(node.hashCode()) {
                    AssistantBlockItem(node)
                }
            }
            // Streaming node
            parser.pendingNodes.forEach { node ->
                key("pending") {
                    AssistantBlockItem(node)
                }
            }
        }
    }
}

@Composable
internal fun AssistantBlockItem(node: MarkdownNode) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(horizontal = 12.dp)
    ) {
        MarkdownNodeView(node)
    }
}

private class IncrementalMarkdownParser {

    /**
     * Stable nodes
     */
    val stableNodes = mutableStateListOf<MarkdownNode>()

    /**
     * Current streaming node
     */
    var pendingNodes by mutableStateOf<List<MarkdownNode>>(emptyList())
        private set

    private var parsedIndex = 0

    /**
     * Unstable markdown buffer
     */
    private val pendingBuffer = StringBuilder()

    fun update(content: String) {
        if (content.length < parsedIndex) {
            reset()
        }
        // Incremental part
        val delta = content.substring(parsedIndex)
        if (delta.isEmpty()) {
            return
        }
        parsedIndex = content.length
        pendingBuffer.append(delta)

        // Incremental parsing
        val result = parseMarkDown(md = pendingBuffer.toString())
        val nodes = result.nodes
        if (nodes.isEmpty()) {
            return
        }

        /**
         * Multi-node, means：
         *
         * Front nodes are stable
         */
        if (nodes.size > 1) {

            val stablePart = nodes.dropLast(1)
            stableNodes.addAll(stablePart)
            pendingNodes = listOf(nodes.last())
            // Delete the consumed portion
            pendingBuffer.delete(0, result.parsedLength)
        } else {
            pendingNodes = nodes
        }
    }

    fun reset() {
        stableNodes.clear()
        pendingNodes = emptyList()
        pendingBuffer.clear()
        parsedIndex = 0
    }
}