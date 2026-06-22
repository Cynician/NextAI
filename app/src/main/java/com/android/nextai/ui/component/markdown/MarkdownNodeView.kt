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
import com.android.nextai.ui.component.markdown.views.BlockQuoteView
import com.android.nextai.ui.component.markdown.views.FencedCodeBlockView
import com.android.nextai.ui.component.markdown.views.HeadingView
import com.android.nextai.ui.component.markdown.views.ListBlockView
import com.android.nextai.ui.component.markdown.views.ParagraphView
import com.android.nextai.ui.component.markdown.views.TableBlockView
import com.android.nextai.ui.component.markdown.views.TextView


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
    val style = MaterialTheme.typography.bodyMedium

    when (node) {

        is MarkdownNode.Text -> TextView(node)

        is MarkdownNode.Paragraph -> ParagraphView(node, colors, style)

        is MarkdownNode.Heading -> HeadingView(node, colors, style)

        is MarkdownNode.ListBlock -> ListBlockView(node, 0)

        is MarkdownNode.BlockQuote -> BlockQuoteView(node, 0)

        is MarkdownNode.FencedCodeBlock -> FencedCodeBlockView(node, true)

        is MarkdownNode.ThematicBreak -> HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        is MarkdownNode.TableBlock -> TableBlockView(node, colors)

        else -> {}
    }
}