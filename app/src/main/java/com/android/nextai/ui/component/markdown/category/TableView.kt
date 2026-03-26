package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils.buildInlineFormatted
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils.drawTableRow

@Composable
fun TableView(
    headers: List<String>,
    rows: List<List<String>>,
    alignments: List<MarkdownElement.Table.Alignment>,
    colors: InlineColors,
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val altRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.10f)
    val textColor = LocalContentColor.current
    val dimTextColor = textColor.copy(alpha = 0.87f)
    val colCount = headers.size.coerceAtLeast(1)
    val density = LocalDensity.current
    val cellPadH = with(density) { 8.dp.toPx() }
    val cellPadV = with(density) { 6.dp.toPx() }
    val dividerWidth = with(density) { 1.dp.toPx() }
    val cornerRadius = with(density) { 8.dp.toPx() }
    val textMeasurer = rememberTextMeasurer()

    val baseTypo = MaterialTheme.typography.bodySmall
    val headerStyle = baseTypo.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        lineHeight = 17.sp
    )
    val cellStyle = baseTypo.copy(fontSize = 12.sp, color = dimTextColor, lineHeight = 17.sp)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
    ) {
        val totalWidth = with(density) { maxWidth.toPx() }
        val colWidth = (totalWidth - dividerWidth * (colCount - 1)) / colCount
        val cellTextWidth = (colWidth - cellPadH * 2).coerceAtLeast(1f).toInt()

        val headerMeasured = remember(headers, cellTextWidth, colors) {
            headers.map { h ->
                textMeasurer.measure(
                    text = buildInlineFormatted(h, colors),
                    style = headerStyle,
                    maxLines = 3,
                    constraints = Constraints(maxWidth = cellTextWidth)
                )
            }
        }
        val rowsMeasured = remember(rows, cellTextWidth, colors) {
            rows.map { row ->
                (0 until colCount).map { ci ->
                    textMeasurer.measure(
                        text = buildInlineFormatted(row.getOrElse(ci) { "" }, colors),
                        style = cellStyle,
                        maxLines = 5,
                        constraints = Constraints(maxWidth = cellTextWidth)
                    )
                }
            }
        }

        val headerRowHeight = remember(headerMeasured) {
            (headerMeasured.maxOfOrNull { it.size.height } ?: 0) + (cellPadV * 2)
        }
        val rowHeights = remember(rowsMeasured) {
            rowsMeasured.map { cells ->
                (cells.maxOfOrNull { it.size.height } ?: 0) + (cellPadV * 2)
            }
        }
        val totalHeight = headerRowHeight + rowHeights.sum() + dividerWidth * rows.size

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { totalHeight.toDp() })
                .drawBehind {
                    drawRoundRect(
                        color = outlineColor,
                        size = Size(totalWidth, totalHeight),
                        cornerRadius = CornerRadius(cornerRadius),
                        style = Stroke(width = dividerWidth)
                    )
                }
        ) {
            var y = 0f

            drawRect(
                color = headerBg,
                topLeft = Offset(0f, 0f),
                size = Size(totalWidth, headerRowHeight)
            )
            drawTableRow(
                headerMeasured,
                colCount,
                colWidth,
                cellPadH,
                cellPadV,
                dividerWidth,
                y,
                headerRowHeight,
                alignments,
                outlineColor
            )
            y += headerRowHeight

            rowsMeasured.forEachIndexed { rowIndex, cells ->
                drawRect(
                    color = outlineColor.copy(alpha = 0.4f),
                    topLeft = Offset(0f, y),
                    size = Size(totalWidth, dividerWidth)
                )
                y += dividerWidth
                val rh = rowHeights[rowIndex]
                if (rowIndex % 2 == 1) drawRect(
                    color = altRowBg,
                    topLeft = Offset(0f, y),
                    size = Size(totalWidth, rh)
                )
                drawTableRow(
                    cells,
                    colCount,
                    colWidth,
                    cellPadH,
                    cellPadV,
                    dividerWidth,
                    y,
                    rh,
                    alignments,
                    outlineColor
                )
                y += rh
            }
        }
    }
}