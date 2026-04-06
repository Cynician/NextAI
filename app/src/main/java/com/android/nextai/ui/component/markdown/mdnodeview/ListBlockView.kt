package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.entity.MarkdownNode

@Composable
fun ListBlockView(
    node: MarkdownNode.ListBlock,
    depth: Int = 0,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Standard.SpacingXs),
        modifier = Modifier.absolutePadding(top = 4.dp)
    ) {
        node.children.forEachIndexed { index, child ->
            ListItemView(
                node = child as MarkdownNode.ListItem,
                depth = depth,
                index = index,
                ordered = node.ordered
            )
        }
    }
}

@Composable
fun ListItemView(node: MarkdownNode.ListItem, depth: Int, index: Int, ordered: Boolean) {

    Row(modifier = Modifier.fillMaxWidth()) {
        val marker = if (ordered) {
            "${index + 1}."
        } else {
            when (depth) {
                0 -> "•"
                1 -> "◦"
                2 -> "▪"
                else -> "▫"
            }
        }

        Text(
            text = marker,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .alignByBaseline()
        )
        Column(
            modifier = Modifier
                .alignByBaseline()
                .weight(1f)
        ) {
            node.children.forEach { child ->
                when (child) {
                    is MarkdownNode.ListBlock -> {
                        ListBlockView(node = child, depth = depth + 1)
                    }

                    else -> {
                        MarkdownNodeView(child)
                    }
                }
            }
        }

    }
}