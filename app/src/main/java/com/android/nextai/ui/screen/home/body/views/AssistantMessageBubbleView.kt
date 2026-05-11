package com.android.nextai.ui.screen.home.body.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.entity.MarkdownNode

@Composable
internal fun AssistantMessageBubble(block: MarkdownNode) {
    Box(
        modifier = Modifier
            .padding(
                top = 4.dp,
                bottom = 4.dp
            )
            .padding(horizontal = 12.dp)
    ) {
        MarkdownNodeView(block)
    }
}

@Composable
internal fun AssistantMessageBubbleList(blocks: List<MarkdownNode>) {
    SelectionContainer() {
        Column {
            blocks.forEach { block ->
                AssistantMessageBubble(block)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}