package com.android.nextai.ui.component.markdown

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.mdnodeview.BlockQuoteView
import com.android.nextai.ui.component.markdown.mdnodeview.FencedCodeBlockView
import com.android.nextai.ui.component.markdown.mdnodeview.HeadingView
import com.android.nextai.ui.component.markdown.mdnodeview.ListBlockView
import com.android.nextai.ui.component.markdown.mdnodeview.ListItemView
import com.android.nextai.ui.component.markdown.mdnodeview.ParagraphView
import com.android.nextai.ui.component.markdown.mdnodeview.TextView
import com.android.nextai.ui.component.markdown.mdnodeview.appendInlineNodes
import com.vladsch.flexmark.ext.tables.TableCell

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
            modifier = Modifier.padding(vertical = Standard.SpacingXs),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Table
        is MarkdownNode.TableBlock -> TableBlockView(node, colors)
        else -> {}
    }


}



@Composable
fun TableBlockView(node: MarkdownNode.TableBlock, colors: InlineColors) {



    val (headers, bodyRows) = remember(node) { // <-- 在组合阶段同步计算
        val headerRows = mutableListOf<MarkdownNode.TableRow>()
        val dataRows = mutableListOf<MarkdownNode.TableRow>()

        node.children.forEach { child ->
            when (child) {
                is MarkdownNode.TableHead -> {
                    headerRows.addAll(child.children.filterIsInstance<MarkdownNode.TableRow>())
                }
                is MarkdownNode.TableBody -> {
                    dataRows.addAll(child.children.filterIsInstance<MarkdownNode.TableRow>())
                }
                else -> { /* Ignore TableSeparator etc. */ }
            }
        }
        //
        Pair(headerRows, dataRows)
    }
    TableView(headers = headers, rows = bodyRows, colors)
}

@Composable
fun TableView(
    headers: List<MarkdownNode.TableRow>,
    rows: List<MarkdownNode.TableRow>,
    colors: InlineColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        headers.forEach { row ->
            TableRowView(row, colors)
        }
        rows.forEach { row ->
            TableRowView(row, colors)
        }
    }
}

@Composable
fun TableRowView(row: MarkdownNode.TableRow, colors: InlineColors) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        row.children.filterIsInstance<MarkdownNode.TableCell>().forEach { cell ->
            key(cell) {
                TableCellView(cell, colors)
            }
        }
    }
}

@Composable
fun TableCellView(cell: MarkdownNode.TableCell, colors: InlineColors) {
    val text = buildAnnotatedString { appendInlineNodes(cell.children, colors) }
    Log.d("MarkdownNodeView", "cell:$text")
    val boxAlign = when (cell.alignment) {
        TableCell.Alignment.CENTER -> Alignment.Center
        TableCell.Alignment.RIGHT -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
    val textAlign = when (cell.alignment) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.LEFT -> TextAlign.Left
        TableCell.Alignment.RIGHT -> TextAlign.Right
        else -> {TextAlign.Center}
    }
    Box(
        modifier = Modifier
            .border(0.5.dp, Color.Gray)
            .padding(8.dp),
        contentAlignment = boxAlign
    ) {
        Text(
            text = text,
            textAlign = textAlign,
            fontWeight = if (cell.header) FontWeight.Bold else FontWeight.Normal
        )
    }
}


