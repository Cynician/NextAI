package com.android.nextai.ui.component.markdown.parser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.parser.MarkdownParser.parseMarkDown

/**
 * Used for incremental parsing in Markdown documents, for each long text with streaming output, it
 * can be parsed as stable nodes or a node in parsing.
 */
class MarkdownIncrementalParser {

    /**
     * Stable nodes
     */
    val stableNodes = mutableStateListOf<MarkdownNode>()

    /**
     * Streaming node
     */
    var pendingNodes by mutableStateOf<List<MarkdownNode>>(emptyList())
        private set

    /**
     * Incremental parsing index
     */
    private var parsedIndex = 0

    /**
     * Unstable markdown buffer
     */
    private val pendingBuffer = StringBuilder()

    /**
     * Full parse
     *
     * Used for:
     * - history messages
     * - database restore
     */
    fun setContent(content: String) {
        reset()
        if (content.isEmpty()) {
            return
        }

        val result = parseMarkDown(content)
        stableNodes.addAll(result.nodes)
        parsedIndex = content.length
    }

    // Incremental update, used for: streaming response
    fun append(fullContent: String) {

        if (fullContent.length < parsedIndex) {
            reset()
        }
        val delta = fullContent.substring(parsedIndex)
        if (delta.isEmpty()) {
            return
        }
        parsedIndex = fullContent.length
        pendingBuffer.append(delta)
        val result = parseMarkDown(
            md = pendingBuffer.toString()
        )
        val nodes = result.nodes
        if (nodes.isEmpty()) {
            return
        }

        // Multiple nodes: front nodes are stable
        if (nodes.size > 1) {
            val stablePart = nodes.dropLast(1)
            stableNodes.addAll(stablePart)
            pendingNodes = listOf(nodes.last())
            pendingBuffer.delete(0, result.parsedLength)
        } else {
            pendingNodes = nodes
        }
    }

    /**
     * Flush streaming buffer
     */
    fun complete() {

        if (pendingBuffer.isEmpty()) {
            return
        }

        val result = parseMarkDown(md = pendingBuffer.toString())
        val nodes = result.nodes
        if (nodes.isNotEmpty()) {
            stableNodes.addAll(nodes)
            pendingNodes = emptyList()
        }
        pendingBuffer.clear()
    }

    fun reset() {
        stableNodes.clear()
        pendingNodes = emptyList()
        pendingBuffer.clear()
        parsedIndex = 0
    }
}