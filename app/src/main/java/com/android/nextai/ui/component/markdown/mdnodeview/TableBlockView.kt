package com.android.nextai.ui.component.markdown.mdnodeview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.vladsch.flexmark.ext.tables.TableCell

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
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val altRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val typography = MaterialTheme.typography
    val minWidthPx = with(density) { 80.dp.toPx() }
    val maxWidthPx = with(density) { 320.dp.toPx() }

    val columnWidths = remember(headers, rows) {
        val allRows = headers + rows

        val colCount = allRows.maxOfOrNull {
            it.children.filterIsInstance<MarkdownNode.TableCell>().size
        } ?: 0

        val widths = FloatArray(colCount)

        allRows.forEach { row ->
            val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()

            cells.forEachIndexed { i, cell ->
                val text = buildAnnotatedString {
                    appendInlineNodes(cell.children, colors)
                }

                val result = textMeasurer.measure(
                    text = text,
                    style = typography.bodySmall,

                    constraints = Constraints(
                        maxWidth = maxWidthPx.toInt()
                    ),

                    maxLines = Int.MAX_VALUE
                )

                widths[i] = maxOf(widths[i], result.size.width.toFloat())
            }
        }

        widths.map {
            it.coerceIn(minWidthPx, maxWidthPx)
        }
    }

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, outlineColor, MaterialTheme.shapes.small)
            .horizontalScroll(rememberScrollState())
    ) {
        headers.forEach { row ->
            TableRowView(row = row,
                isHeader = true,
                colors = colors,
                background = headerBg,
                outlineColor= outlineColor,
                columnWidths = columnWidths)
        }
        rows.forEachIndexed  { index, row ->
            TableRowView(
                row = row,
                isHeader = false,
                colors = colors,
                background = if (index % 2 == 1) altRowBg else Color.Transparent,
                outlineColor= outlineColor,
                columnWidths = columnWidths,
            )
        }
    }
}

@Composable
fun TableRowView(
    row: MarkdownNode.TableRow,
    isHeader: Boolean,
    colors: InlineColors,
    background: Color = Color.Transparent,
    outlineColor: Color,
    columnWidths: List<Float>,
) {
    val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()

    val density = LocalDensity.current
    Column(
        modifier = Modifier.background(background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = if (isHeader) 4.dp else 0.dp)
                .height(IntrinsicSize.Min)
                .background(background)
        ) {
            cells.forEachIndexed  { index, cell ->
                val widthDp = with(density) {
                    columnWidths.getOrElse(index) { 100f }.toDp()
                }
                TableCellView(cell, colors, modifier = Modifier.width(widthDp))
                // inner column line
                if (index != cells.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight()
                            .background(outlineColor.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun TableCellView(
    cell: MarkdownNode.TableCell,
    colors: InlineColors,
    modifier: Modifier = Modifier
) {
    val text = buildAnnotatedString { appendInlineNodes(cell.children, colors) }
    Log.d("MarkdownNodeView", "cell:$text")
    val boxAlign = when (cell.alignment) {
        TableCell.Alignment.LEFT -> Alignment.CenterStart
        TableCell.Alignment.CENTER -> Alignment.Center
        TableCell.Alignment.RIGHT -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    val textAlign = when (cell.alignment) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.LEFT -> TextAlign.Left
        TableCell.Alignment.RIGHT -> TextAlign.Right
        else -> {TextAlign.Center}
    }
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = boxAlign
    ) {
        Text(
            text = text,
            maxLines = Int.MAX_VALUE,
            textAlign = textAlign,
            fontWeight = if (cell.header) FontWeight.Bold else FontWeight.Normal,
            softWrap = true
        )
    }
}