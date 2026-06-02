package com.android.nextai.ui.component.markdown

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.mdnodeview.BlockQuoteView
import com.android.nextai.ui.component.markdown.mdnodeview.FencedCodeBlockView
import com.android.nextai.ui.component.markdown.mdnodeview.HeadingView
import com.android.nextai.ui.component.markdown.mdnodeview.ListBlockView
import com.android.nextai.ui.component.markdown.mdnodeview.ListItemView
import com.android.nextai.ui.component.markdown.mdnodeview.ParagraphView
import com.android.nextai.ui.component.markdown.mdnodeview.TableBlockView
import com.android.nextai.ui.component.markdown.mdnodeview.TextView

@Composable
fun MarkdownNodeView(node: MarkdownNode) {

    val scheme = MaterialTheme.colorScheme
    val colors by remember {
        mutableStateOf(
            InlineColors(
                codeBg = scheme.surfaceVariant.copy(alpha = 0.5f),
                highlightBg = scheme.primary.copy(alpha = 0.3f),
                mathColor = scheme.primary
            )
        )
    }
    when (node) {
        // Block
        is MarkdownNode.Paragraph -> ParagraphView(node, colors)
        is MarkdownNode.Heading -> HeadingView(node, colors)
        is MarkdownNode.ListBlock -> ListBlockView(node, 0)
        is MarkdownNode.ListItem -> ListItemView(
            node,
            depth = node.depth,
            index = node.index,
            node.ordered
        )
        is MarkdownNode.BlockQuote -> BlockQuoteView(node, 0)
        // Inline
        is MarkdownNode.Text -> TextView(node)
        is MarkdownNode.FencedCodeBlock -> FencedCodeBlockView(node, true)
        is MarkdownNode.ThematicBreak -> HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        // Table
        is MarkdownNode.TableBlock -> TableBlockView(node, colors)
        else -> {}
    }
}


