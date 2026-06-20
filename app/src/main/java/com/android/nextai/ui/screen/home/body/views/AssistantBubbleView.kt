package com.android.nextai.ui.screen.home.body.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.parser.MarkdownIncrementalParser

@Composable
internal fun AssistantBubbleView(
    parser: MarkdownIncrementalParser,
) {

    SelectionContainer {

        Column{

            parser.stableNodes.forEach { node ->
                AssistantBlockItem(node)
            }

            parser.pendingNodes.forEach { node ->
                AssistantBlockItem(node)
            }
        }
    }
}

@Composable
internal fun AssistantBlockItem(
    node: MarkdownNode
) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
    ) {
        MarkdownNodeView(node)
    }
}