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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.vladsch.flexmark.ext.tables.TableCell
import com.hrm.latex.renderer.LatexAutoWrap
import com.hrm.latex.renderer.model.LatexConfig
import kotlin.math.max

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
                else -> { /* Ignore */ }
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

    val headerHorizontalPadding = 12.dp
    val contentHorizontalPadding = 12.dp

    val allRows = headers + rows
    val colCount = remember(allRows) {
        allRows.maxOfOrNull { it.children.filterIsInstance<MarkdownNode.TableCell>().size } ?: 0
    }

    // 为每一列定义合适的基础宽度和最大宽度边界，长公式在此边界内自动换行
    val fixedColumnWidths = remember(colCount) {
        // 第一列普通函数名可以窄一点，中间公式列和最后一列收敛域由于有长公式，直接给予充足的固定宽度空间
        List(colCount) { index ->
            when (index) {
                0 -> 240.dp
                1 -> 240.dp
                else -> 240.dp
            }
        }
    }

    val tableShape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .clip(tableShape)
            .border(width = 0.5.dp, color = outlineColor, shape = tableShape)
    ) {
        headers.forEach { row ->
            SyncTableRowWrapper(
                row = row,
                isHeader = true,
                colors = colors,
                style = currentStyle,
                background = headerBg,
                outlineColor = outlineColor,
                hPadding = headerHorizontalPadding,
                vPadding = 12.dp,
                colCount = colCount,
                columnWidths = fixedColumnWidths
            )
        }
        rows.forEachIndexed { index, row ->
            SyncTableRowWrapper(
                row = row,
                isHeader = false,
                colors = colors,
                style = currentStyle,
                background = contentBg,
                outlineColor = outlineColor,
                hPadding = contentHorizontalPadding,
                vPadding = 12.dp,
                isLastRow = index == rows.lastIndex,
                colCount = colCount,
                columnWidths = fixedColumnWidths
            )
        }
    }
}

/**
 * 【终极核心组件】：利用高级 SubcomposeLayout 代替标准的 Row。
 * 它能够分步执行：第一步测量拿到所有单元格的真实爆发高度，第二步强行拉伸整行，从物理层面上粉碎重叠错位。
 */
@Composable
fun SyncTableRowWrapper(
    row: MarkdownNode.TableRow,
    isHeader: Boolean,
    colors: InlineColors,
    style: TextStyle,
    background: Color,
    outlineColor: Color,
    hPadding: Dp,
    vPadding: Dp,
    colCount: Int,
    columnWidths: List<Dp>,
    isLastRow: Boolean = false,
) {
    val cells = row.children.filterIsInstance<MarkdownNode.TableCell>()

    Column(modifier = Modifier.background(background)) {
        // 使用 SubcomposeLayout 进行精准的“高度夹逼测量”
        SubcomposeLayout { constraints ->

            // 1. 预先测量这一行所有的单元格，让他们自由释放高度
            val placeables = (0 until colCount).map { index ->
                val cell = cells.getOrNull(index)
                val cellWidthPx = columnWidths[index].roundToPx()

                // 使用 subcompose 独立开辟插槽进行探测
                subcompose("cell_$index") {
                    if (cell != null) {
                        TableCellView(
                            cell = cell,
                            colors = colors,
                            style = style,
                            isHeader = isHeader,
                            hPadding = hPadding,
                            vPadding = vPadding
                        )
                    } else {
                        Box(modifier = Modifier.width(columnWidths[index]))
                    }
                }.map { measurable ->
                    // 强制给予规定的宽度，高度保持自适应包裹
                    measurable.measure(
                        Constraints(
                            minWidth = cellWidthPx,
                            maxWidth = cellWidthPx,
                            minHeight = 0,
                            maxHeight = Constraints.Infinity
                        )
                    )
                }
            }

            // 2. 核心计算：找出当前行所有单元格中，由于 Latex 展开导致的最深、最高的那一个高度
            var maxRowHeight = 0
            placeables.flatten().forEach { placeable ->
                maxRowHeight = max(maxRowHeight, placeable.height)
            }

            // 兜底保底行高，防止出现极端 0 高度
            if (maxRowHeight == 0) {
                maxRowHeight = 45.dp.roundToPx()
            }

            // 3. 重新测量：用刚刚找出来的 maxRowHeight 制造一个绝对强制的“硬约束”，重新注入给每一个单元格
            val finalPlaceables = (0 until colCount).map { index ->
                val cell = cells.getOrNull(index)
                val cellWidthPx = columnWidths[index].roundToPx()

                subcompose("final_cell_$index") {
                    if (cell != null) {
                        TableCellView(
                            cell = cell,
                            colors = colors,
                            style = style,
                            isHeader = isHeader,
                            hPadding = hPadding,
                            vPadding = vPadding,
                            modifier = Modifier.fillMaxHeight() // 允许占满最高高度
                        )
                    } else {
                        Box(modifier = Modifier.width(columnWidths[index]).fillMaxHeight())
                    }
                }.map { measurable ->
                    // 关键改动：宽高全部锁死为当前行的最大对齐尺寸！
                    measurable.measure(Constraints.fixed(cellWidthPx, maxRowHeight))
                }
            }

            // 4. 计算整行的总宽度（加上分割线）
            val totalWidth = columnWidths.sumOf { it.roundToPx() } + ((colCount - 1) * 0.5.dp.roundToPx())

            // 5. 开始物理排版绘制
            layout(totalWidth, maxRowHeight) {
                var xPosition = 0
                val dividerWidthPx = 0.5.dp.roundToPx()

                finalPlaceables.forEachIndexed { index, list ->
                    list.forEach { placeable ->
                        placeable.placeRelative(xPosition, 0)
                        xPosition += placeable.width
                    }

                    // 顺手绘制垂直分割线，同样强制拉满高度
                    if (index < colCount - 1) {
                        subcompose("div_$index") {
                            Box(modifier = Modifier.width(0.5.dp).height(maxRowHeight.toDp()).background(outlineColor))
                        }.forEach { measurable ->
                            measurable.measure(Constraints.fixed(dividerWidthPx, maxRowHeight)).placeRelative(xPosition, 0)
                        }
                        xPosition += dividerWidthPx
                    }
                }
            }
        }

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
    val textAlign = when (cell.alignment) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.RIGHT -> TextAlign.Right
        else -> TextAlign.Left
    }

    val boxAlign = when (cell.alignment) {
        TableCell.Alignment.CENTER -> Alignment.Center
        TableCell.Alignment.RIGHT -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }

    // 每一格的最外层：完美支持外部注入的约束拉伸
    Box(
        modifier = modifier.padding(horizontal = hPadding, vertical = vPadding),
        contentAlignment = Alignment.Center // 终极垂直居中保障
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            contentAlignment = boxAlign
        ) {
            val hasMath = cell.children.any {
                it is MarkdownNode.InlineMath || it.javaClass.simpleName.contains("Math")
            }

            if (!hasMath) {
                val text = buildAnnotatedString { appendInlineNodes(cell.children, colors) }
                Text(
                    text = text,
                    style = style,
                    maxLines = Int.MAX_VALUE,
                    textAlign = textAlign,
                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                    softWrap = true
                )
            } else {
                val fullLatexString = remember(cell.children) {
                    buildString {
                        cell.children.forEach { child ->
                            if (child is MarkdownNode.InlineMath || child.javaClass.simpleName.contains("Math")) {
                                val formula = try {
                                    val field = child.javaClass.getDeclaredField("formula")
                                    field.isAccessible = true
                                    field.get(child) as String
                                } catch (e: Exception) {
                                    child.toString()
                                }
                                if (!formula.startsWith("$")) append("$")
                                append(formula)
                                if (!formula.endsWith("$")) append("$")
                            } else {
                                val rawText = try {
                                    val field = child.javaClass.getDeclaredField("text")
                                    field.isAccessible = true
                                    field.get(child) as String
                                } catch (e: Exception) {
                                    ""
                                }
                                if (rawText.isNotEmpty()) {
                                    append("\\text{ $rawText }")
                                }
                            }
                        }
                    }
                }

                // 核心：直接调用。此时外层 SubcomposeLayout 已经给定了精准的单元格 Constraints 宽度，
                // 内部的 BoxWithConstraints 能够立刻知道应该在哪一个边界开始强制截断换行。
                // 换行生成的高度会被外部的 SubcomposeLayout 强行同步给同行的其他单元格。
                LatexAutoWrap(
                    latex = fullLatexString.replace("\\|", "|"),
                    modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                    config = LatexConfig(
                        fontSize = style.fontSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}