package com.android.nextai.ui.component.markdown.mdnodeview

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.vladsch.flexmark.ext.tables.TableCell

@Composable
fun TableBlockView(node: MarkdownNode.TableBlock, colors: InlineColors) {
    val (headers, bodyRows) = remember(node) {
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

                else -> { /* Ignore */
                }
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
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val contentBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val currentStyle = MaterialTheme.typography.bodySmall

    val headerHorizontalPadding = 16.dp
    val contentHorizontalPadding = 16.dp

    val maxTextWidthPx = remember(currentStyle) {
        val sampleText = "一二三四五六七八九十Android"
        textMeasurer.measure(sampleText, currentStyle).size.width.toFloat()
    }

    val maxHeaderCellWidthPx = maxTextWidthPx + with(density) { headerHorizontalPadding.toPx() * 2 }
    val maxContentCellWidthPx =
        maxTextWidthPx + with(density) { contentHorizontalPadding.toPx() * 2 }
    val minWidthPx = with(density) { 100.dp.toPx() }

    val columnWidths = remember(headers, rows) {
        val allRows = headers + rows
        val colCount = allRows.maxOfOrNull {
            it.children.filterIsInstance<MarkdownNode.TableCell>().size
        } ?: 0

        val widths = FloatArray(colCount)

        headers.forEach { row ->
            val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()
            cells.forEachIndexed { i, cell ->
                val text = buildAnnotatedString { appendInlineNodes(cell.children, colors) }
                val result = textMeasurer.measure(
                    text = text,
                    style = currentStyle,
                    constraints = Constraints(maxWidth = maxTextWidthPx.toInt()),
                    maxLines = Int.MAX_VALUE
                )
                val cellWidth =
                    result.size.width.toFloat() + with(density) { headerHorizontalPadding.toPx() * 2 }
                widths[i] = maxOf(widths[i], cellWidth)
            }
        }

        rows.forEach { row ->
            val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()
            cells.forEachIndexed { i, cell ->
                val text = buildAnnotatedString { appendInlineNodes(cell.children, colors) }
                val result = textMeasurer.measure(
                    text = text,
                    style = currentStyle,
                    constraints = Constraints(maxWidth = maxTextWidthPx.toInt()),
                    maxLines = Int.MAX_VALUE
                )
                val cellWidth =
                    result.size.width.toFloat() + with(density) { contentHorizontalPadding.toPx() * 2 }
                widths[i] = maxOf(widths[i], cellWidth)
            }
        }

        widths.map { it.coerceIn(minWidthPx, maxOf(maxHeaderCellWidthPx, maxContentCellWidthPx)) }
    }

    /** Define the rounded shape. **/
    val tableShape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
            .clip(tableShape)
            .border(width = 0.5.dp, color = outlineColor, shape = tableShape)
    ) {

        headers.forEachIndexed { index, row ->
            TableRowView(
                row = row,
                isHeader = true,
                colors = colors,
                style = currentStyle,
                background = headerBg,
                outlineColor = outlineColor,
                columnWidths = columnWidths,
                hPadding = headerHorizontalPadding,
                vPadding = 14.dp,
            )
        }
        rows.forEachIndexed { index, row ->
            TableRowView(
                row = row,
                isHeader = false,
                colors = colors,
                style = currentStyle,
                background = contentBg,
                outlineColor = outlineColor,
                columnWidths = columnWidths,
                hPadding = contentHorizontalPadding,
                vPadding = 10.dp,
                isLastRow = index == rows.lastIndex,
            )
        }
    }
}

@Composable
fun TableRowView(
    row: MarkdownNode.TableRow,
    isHeader: Boolean,
    colors: InlineColors,
    style: TextStyle,
    background: Color,
    outlineColor: Color,
    columnWidths: List<Float>,
    hPadding: Dp,
    vPadding: Dp,
    isLastRow: Boolean = false,
) {
    val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()
    val density = LocalDensity.current

    Column(
        modifier = Modifier.background(background)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .background(background)
        ) {

            cells.forEachIndexed { index, cell ->
                val widthDp = with(density) {
                    columnWidths.getOrElse(index) { 100f }.toDp()
                }

                TableCellView(
                    cell = cell,
                    colors = colors,
                    style = style,
                    isHeader = isHeader,
                    hPadding = hPadding,
                    vPadding = vPadding,
                    modifier = Modifier
                        .width(widthDp)
                        .fillMaxHeight()
                )

                /**
                 * Vertical inner/outer border lines: If it is the last column, do not draw the
                 * right border (handled by the outer border).
                 */
                if (index < cells.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight()
                            .background(outlineColor)
                    )
                }
            }
        }

        /**
         * Horizontal divider: If it is not the last row, draw the divider normally.
         */
        if (!isLastRow) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(outlineColor)
            )
        }
    }
}

@Composable
fun TableCellView(
    cell: MarkdownNode.TableCell,
    colors: InlineColors,
    style: TextStyle,
    isHeader: Boolean,
    hPadding: Dp,
    vPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val text = buildAnnotatedString { appendInlineNodes(cell.children, colors) }

    /** All cell containers must be centered **/
    val boxAlign = Alignment.Center

    val textAlign = when (cell.alignment) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.LEFT -> TextAlign.Left
        TableCell.Alignment.RIGHT -> TextAlign.Right
        else -> TextAlign.Left
    }

    Box(
        modifier = modifier.padding(horizontal = hPadding, vertical = vPadding),
        contentAlignment = boxAlign
    ) {
        Text(
            text = text,
            style = style,
            maxLines = Int.MAX_VALUE,
            textAlign = textAlign,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            softWrap = true
        )
    }
}