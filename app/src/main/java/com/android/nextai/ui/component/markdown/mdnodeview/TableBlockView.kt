package com.android.nextai.ui.component.markdown.mdnodeview

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
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import kotlin.math.max
import kotlin.math.min

@Composable
fun TableBlockView(node: MarkdownNode.TableBlock, colors: InlineColors) {
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

    val allRows = headers + rows
    val colCount = remember(allRows) {
        allRows.maxOfOrNull { it.children.filterIsInstance<MarkdownNode.TableCell>().size } ?: 0
    }

    // 设置更具弹性的阈值范围：保底 90dp，上限扩容至 340dp 确保巨型公式舒展
    val minCellWidth = 90.dp
    val maxCellWidth = 120.dp
    val tableShape = RoundedCornerShape(8.dp)

    SubcomposeLayout(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .clip(tableShape)
            .border(width = 0.5.dp, color = outlineColor, shape = tableShape)
    ) { constraints ->

        val colMaxWidthsPx = IntArray(colCount) { minCellWidth.roundToPx() }
        val maxConstraintWidthPx = maxCellWidth.roundToPx()

        // 1. 【核心修复】探测阶段：回归无限制自由度
        (0 until colCount).forEach { colIdx ->
            allRows.forEachIndexed { rowIdx, row ->
                val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()
                val cell = cells.getOrNull(colIdx)
                if (cell != null) {
                    val placeables = subcompose("probe_${rowIdx}_${colIdx}") {
                        TableCellContent(cell = cell, colors = colors, style = currentStyle, isHeader = rowIdx < headers.size)
                    }.map {
                        // 👈 彻底移除写死的最大宽度钳制！用无界约束测出巨型公式的最自然舒适平铺长宽
                        it.measure(Constraints())
                    }

                    placeables.forEach {
                        colMaxWidthsPx[colIdx] = max(colMaxWidthsPx[colIdx], it.width)
                    }
                }
            }
            // 测出最高物理尺寸后，再在这里安全筑起最高上限防火墙
            colMaxWidthsPx[colIdx] = min(colMaxWidthsPx[colIdx], maxConstraintWidthPx)
        }

        val computedColumnWidths = colMaxWidthsPx.map { it.toDp() }

        // 2. 实体排版阶段
        val contentPlaceables = subcompose("table_body") {
            Column {
                headers.forEach { row ->
                    SyncTableRowWrapper(
                        row = row, isHeader = true, colors = colors, style = currentStyle,
                        background = headerBg, outlineColor = outlineColor, colCount = colCount,
                        columnWidths = computedColumnWidths
                    )
                }
                rows.forEachIndexed { index, row ->
                    SyncTableRowWrapper(
                        row = row, isHeader = false, colors = colors, style = currentStyle,
                        background = contentBg, outlineColor = outlineColor, colCount = colCount,
                        columnWidths = computedColumnWidths, isLastRow = index == rows.lastIndex
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
fun SyncTableRowWrapper(
    row: MarkdownNode.TableRow,
    isHeader: Boolean,
    colors: InlineColors,
    style: TextStyle,
    background: Color,
    outlineColor: Color,
    colCount: Int,
    columnWidths: List<Dp>,
    isLastRow: Boolean = false,
) {
    val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()

    Column(modifier = Modifier.background(background)) {
        SubcomposeLayout { constraints ->

            // 1. 测量拿到此列宽下的真实行高
            val placeables = (0 until colCount).map { index ->
                val cell = cells.getOrNull(index)
                val cellWidthPx = columnWidths[index].roundToPx()

                subcompose("cell_${index}") {
                    Box {
                        if (cell != null) {
                            TableCellContent(cell = cell, colors = colors, style = style, isHeader = isHeader)
                        }
                    }
                }.map { measurable ->
                    measurable.measure(Constraints(minWidth = cellWidthPx, maxWidth = cellWidthPx, minHeight = 0, maxHeight = Constraints.Infinity))
                }
            }

            val maxRowHeight = placeables.flatten().maxOfOrNull { it.height } ?: 45.dp.roundToPx()

            // 2. 强力锁定，纵向拉伸对齐
            val finalPlaceables = (0 until colCount).map { index ->
                val cell = cells.getOrNull(index)
                val cellWidthPx = columnWidths[index].roundToPx()

                subcompose("final_cell_$index") {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center // 居中锚定
                    ) {
                        if (cell != null) {
                            TableCellContent(cell = cell, colors = colors, style = style, isHeader = isHeader)
                        }
                    }
                }.map { measurable ->
                    measurable.measure(Constraints.fixed(cellWidthPx, maxRowHeight))
                }
            }

            val totalWidth = columnWidths.sumOf { it.roundToPx() } + ((colCount - 1) * 0.5.dp.roundToPx())

            layout(totalWidth, maxRowHeight) {
                var xPosition = 0
                val dividerWidthPx = 0.5.dp.roundToPx()

                finalPlaceables.forEachIndexed { index, list ->
                    list.forEach { placeable ->
                        placeable.placeRelative(xPosition, 0)
                        xPosition += placeable.width
                    }
                    if (index < colCount - 1) {
                        subcompose("div_$index") {
                            Box(modifier = Modifier.width(0.5.dp).height(maxRowHeight.toDp()).background(outlineColor))
                        }.forEach { it.measure(Constraints.fixed(dividerWidthPx, maxRowHeight)).placeRelative(xPosition, 0) }
                        xPosition += dividerWidthPx
                    }
                }
            }
        }

        if (!isLastRow) {
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(outlineColor))
        }
    }
}

@Composable
fun TableCellContent(
    cell: MarkdownNode.TableCell,
    colors: InlineColors,
    style: TextStyle,
    isHeader: Boolean
) {
    val latexMeasurer = rememberLatexMeasurer()

    val inlineContentMap = remember(cell.children) { mutableStateMapOf<String, InlineTextContent>() }
    val density = LocalDensity.current
    val maxCellContentWidth = 120.dp - 24.dp
    val annotatedString = remember(cell.children) {
        buildAnnotatedString {
            appendInlineNodes(
                nodes = cell.children,
                colors = colors,
                style = style,
                density = density,
                latexMeasurer = latexMeasurer,
                inlineContentMap = inlineContentMap,
                maxContentWidth = maxCellContentWidth
            )
        }
    }

    CompositionLocalProvider(LocalDensity provides density) {
        Text(
            text = annotatedString,
            style = style,
            textAlign = TextAlign.Left,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            inlineContent = inlineContentMap,
            softWrap = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}