package com.android.nextai.ui.component.markdown.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.appendInlineNodes
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.LatexRenderParams
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import kotlin.math.max
import kotlin.math.min

// ==========================================
// UI Constants
// ==========================================
private val TABLE_SHAPE = RoundedCornerShape(8.dp)
private val TABLE_BORDER_WIDTH = 0.5.dp
private val TABLE_PADDING_VERTICAL = 8.dp

private val CELL_MIN_WIDTH = 90.dp
private val CELL_MAX_WIDTH = 240.dp
private val CELL_PADDING_HORIZONTAL = 12.dp
private val CELL_PADDING_VERTICAL = 12.dp

// Maximum cell content width (for LaTeX or multimedia computing)
private val MAX_CELL_CONTENT_WIDTH = CELL_MAX_WIDTH - (CELL_PADDING_HORIZONTAL * 2)
private val DEFAULT_ROW_HEIGHT = 45.dp


@Composable
fun TableBlockView(
    node: MarkdownNode.TableBlock,
    colors: InlineColors
) {
    val (headers, bodyRows) = remember(node) {
        val headerRows = mutableListOf<MarkdownNode.TableRow>()
        val dataRows = mutableListOf<MarkdownNode.TableRow>()
        node.children.forEach { child ->
            when (child) {
                is MarkdownNode.TableHead -> headerRows.addAll(child.children.filterIsInstance<MarkdownNode.TableRow>())
                is MarkdownNode.TableBody -> dataRows.addAll(child.children.filterIsInstance<MarkdownNode.TableRow>())
                else -> {}
            }
        }
        Pair(headerRows, dataRows)
    }
    TableView(headers = headers, rows = bodyRows, colors = colors)
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
    val currentStyle = MaterialTheme.typography.bodySmall

    val allRows = remember(headers, rows) { headers + rows }
    val colCount = remember(allRows) {
        allRows.maxOfOrNull { it.children.filterIsInstance<MarkdownNode.TableCell>().size } ?: 0
    }

    SubcomposeLayout(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = TABLE_PADDING_VERTICAL)
            .clip(TABLE_SHAPE)
            .border(width = TABLE_BORDER_WIDTH, color = outlineColor, shape = TABLE_SHAPE)
    ) { constraints ->

        val minCellWidthPx = CELL_MIN_WIDTH.roundToPx()
        val maxCellWidthPx = CELL_MAX_WIDTH.roundToPx()
        val colMaxWidthsPx = IntArray(colCount) { minCellWidthPx }

        // 1. Detection phase: Calculate the maximum width of each column.
        (0 until colCount).forEach { colIdx ->
            allRows.forEachIndexed { rowIdx, row ->
                val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()
                val cell = cells.getOrNull(colIdx)
                if (cell != null) {
                    // 优化：提供稳定的 slotId 防止不必要的丢弃与重建
                    val placeables = subcompose("probe_${rowIdx}_${colIdx}") {
                        TableCell(
                            cell = cell,
                            colors = colors,
                            style = currentStyle,
                            isHeader = rowIdx < headers.size
                        )
                    }.map { it.measure(Constraints()) }

                    placeables.forEach {
                        colMaxWidthsPx[colIdx] = max(colMaxWidthsPx[colIdx], it.width)
                    }
                }
            }
            colMaxWidthsPx[colIdx] = min(colMaxWidthsPx[colIdx], maxCellWidthPx)
        }

        val computedColumnWidths = colMaxWidthsPx.map { it.toDp() }

        // 2. Layout stage: Render the entire table.
        val contentPlaceables = subcompose("table_body") {
            Column {
                headers.forEach { row ->
                    TableRow(
                        row = row, isHeader = true, colors = colors, style = currentStyle,
                        background = headerBg, outlineColor = outlineColor, colCount = colCount,
                        columnWidths = computedColumnWidths
                    )
                }
                rows.forEachIndexed { index, row ->
                    TableRow(
                        row = row, isHeader = false, colors = colors, style = currentStyle,
                        background = contentBg, outlineColor = outlineColor, colCount = colCount,
                        columnWidths = computedColumnWidths,
                    )
                }
            }
        }.map { it.measure(constraints) }

        val totalTableWidth = contentPlaceables.maxOfOrNull { it.width } ?: 0
        val totalTableHeight = contentPlaceables.maxOfOrNull { it.height } ?: 0

        layout(totalTableWidth, totalTableHeight) {
            contentPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}

@Composable
fun TableRow(
    row: MarkdownNode.TableRow,
    isHeader: Boolean,
    colors: InlineColors,
    style: TextStyle,
    background: Color,
    outlineColor: Color,
    colCount: Int,
    columnWidths: List<Dp>,
) {
    val cells = remember(row) { row.children.filterIsInstance<MarkdownNode.TableCell>() }

    Column(
        modifier = Modifier
            .background(background)
            .drawBehind {
                // Horizontal Line
                val stroke = TABLE_BORDER_WIDTH.toPx()
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = stroke
                )
            }
    ) {

        SubcomposeLayout { constraints ->
            val dividerWidthPx = TABLE_BORDER_WIDTH.roundToPx()

            // 1. Preliminary Measurement: Get the highest row height actually needed for each cell in the current row.
            val cellHeights = (0 until colCount).map { index ->
                val cell = cells.getOrNull(index)
                val cellWidthPx = columnWidths[index].roundToPx()

                val measurables = subcompose("cell_measure_${index}") {
                    if (cell != null) {
                        TableCell(cell = cell, colors = colors, style = style, isHeader = isHeader)
                    }
                }
                measurables.maxOfOrNull {
                    it.measure(Constraints(minWidth = cellWidthPx, maxWidth = cellWidthPx)).height
                } ?: 0
            }

            val maxRowHeight = max(cellHeights.maxOrNull() ?: 0, DEFAULT_ROW_HEIGHT.roundToPx())

            // 2. Locking stage: stretch and fill alignment.
            val finalPlaceables = (0 until colCount).map { index ->
                val cell = cells.getOrNull(index)
                val cellWidthPx = columnWidths[index].roundToPx()

                subcompose("cell_final_${index}") {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell != null) {
                            TableCell(cell = cell, colors = colors, style = style, isHeader = isHeader)
                        }
                    }
                }.map { it.measure(Constraints.fixed(cellWidthPx, maxRowHeight)) }
            }

            //  Render the divider's Measurables
            val dividerPlaceables = (0 until colCount - 1).map { index ->
                subcompose("div_${index}") {
                    Box(modifier = Modifier.width(TABLE_BORDER_WIDTH).height(maxRowHeight.toDp()).background(outlineColor))
                }.map { it.measure(Constraints.fixed(dividerWidthPx, maxRowHeight)) }
            }

            val totalWidth = columnWidths.sumOf { it.roundToPx() } + ((colCount - 1) * dividerWidthPx)

            // 3. Placement stage.
            layout(totalWidth, maxRowHeight) {
                var xPosition = 0
                finalPlaceables.forEachIndexed { index, placeables ->
                    placeables.forEach { placeable ->
                        placeable.placeRelative(xPosition, 0)
                        xPosition += placeable.width
                    }
                    if (index < colCount - 1) {
                        dividerPlaceables.getOrNull(index)?.forEach { divPlaceable ->
                            divPlaceable.placeRelative(xPosition, 0)
                            xPosition += divPlaceable.width
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCell(
    cell: MarkdownNode.TableCell,
    colors: InlineColors,
    style: TextStyle,
    isHeader: Boolean
) {
    val density = LocalDensity.current

    // 1. LaTeX params.
    val latexRenderParams = LatexRenderParams(
        latexMeasurer = rememberLatexMeasurer(),
        inlineContentMap = remember(cell.children) { mutableStateMapOf() },
        maxTableCellContentWidth = MAX_CELL_CONTENT_WIDTH
    )

    // 2. Constructing rich texts.
    val annotatedString = remember(cell.children, colors, style) {
        buildAnnotatedString {
            appendInlineNodes(
                nodes = cell.children,
                colors = colors,
                style = style,
                density = density,
                latexRenderParams = latexRenderParams
            )
        }
    }

    // 3. Render.
    CompositionLocalProvider(LocalDensity provides density) {
        Text(
            text = annotatedString,
            inlineContent = latexRenderParams.inlineContentMap,
            style = style,
            textAlign = TextAlign.Left,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            softWrap = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CELL_PADDING_HORIZONTAL, vertical = CELL_PADDING_VERTICAL)
        )
    }
}