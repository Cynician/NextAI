package com.android.nextai.ui.component.markdown.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.utils.MarkdownNodeUtils.parseMarkDown

class IncrementalMarkdownParser {

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

    /**
     * Stream completed
     *
     * Flush all remaining content.
     */
    fun complete() {

        if (pendingBuffer.isEmpty()) {
            return
        }

        val result = parseMarkDown(
            md = pendingBuffer.toString()
        )

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