package com.android.nextai.ui.component.markdown

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.LatexRenderParams
import com.android.nextai.ui.component.markdown.views.InlineMathView
import com.android.nextai.ui.theme.MapleMonoFontFamily
import com.hrm.latex.renderer.model.LatexConfig


fun AnnotatedString.Builder.appendInlineNodes(
    nodes: List<MarkdownNode>,
    colors: InlineColors,
    style: TextStyle,
    density: Density,
    latexRenderParams: LatexRenderParams?,
) {

    nodes.forEachIndexed { index, node ->

        when (node) {
            is MarkdownNode.Text -> append(node.text)

            is MarkdownNode.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlineNodes(
                    nodes = node.children,
                    colors = colors,
                    style = style,
                    density = density,
                    latexRenderParams = latexRenderParams
                )
            }

            is MarkdownNode.Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlineNodes(
                    nodes = node.children,
                    colors = colors,
                    style = style,
                    density = density,
                    latexRenderParams = latexRenderParams
                )
            }

            is MarkdownNode.InlineCode -> withStyle(
                SpanStyle(
                    fontFamily = MapleMonoFontFamily, background = colors.codeBg, fontSize = 12.sp
                )
            ) {
                append(" ${node.code} ")
            }

            is MarkdownNode.Link -> {
                pushStringAnnotation(tag = "URL", annotation = node.url)
                withStyle(
                    SpanStyle(
                        color = colors.mathColor, textDecoration = TextDecoration.Underline
                    )
                ) {
                    appendInlineNodes(
                        nodes = node.children,
                        colors = colors,
                        style = style,
                        density = density,
                        latexRenderParams = latexRenderParams,
                    )
                }
                pop()
            }

            is MarkdownNode.InlineMath -> {
                val formula = node.formula

                // Unified Formula Format (Clean Prefixes and Postfixes)
                val latexFormula = formula.replace("\\|", "|").let {
                    if (!it.startsWith("$")) "$$it$" else it // 修复后：正确的双边包裹
                }
                val id = "math_${latexFormula.hashCode()}_$index"

                if (latexRenderParams != null) {

                    val maxTableCellContentWidth = latexRenderParams.maxTableCellContentWidth
                    val latexMeasurer = latexRenderParams.latexMeasurer
                    val inlineContentMap = latexRenderParams.inlineContentMap

                    // 2. Using the meter, pure synchronous memory measurements can be performed directly on the first frame!
                    val dimensions = latexMeasurer.measure(
                        latex = latexFormula, config = LatexConfig(fontSize = style.fontSize)
                    )

                    if (dimensions != null) {
                        with(density) {

                            // 3. Convert the pixel size obtained synchronously (including padding) 1:1 to SP.
                            val rawWidthDp = dimensions.widthPx.toDp()
                            val exactHeightSp = dimensions.heightPx.toSp()

                            // For limit table cell's width and make it scrollable.
                            val isTooLong =
                                rawWidthDp > (maxTableCellContentWidth ?: Int.MAX_VALUE.dp)

                            val finalPlaceholderWidth =
                                if (isTooLong && maxTableCellContentWidth != null)
                                    maxTableCellContentWidth else rawWidthDp

                            inlineContentMap[id] = InlineTextContent(
                                Placeholder(
                                    width = finalPlaceholderWidth.toSp(),
                                    height = exactHeightSp,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                )
                            ) {
                                InlineMathView(
                                    isFormulaTooLong = isTooLong,
                                    formula = latexFormula,
                                    style = style,
                                )
                            }
                            // Write placeholders.
                            appendInlineContent(id, "[math]")
                        }
                    } else {
                        // If the measurement fails, a regular character size is used as a safety net to prevent crashes.
                        println("Latex measure fail, formula: $latexFormula")
                        append(latexFormula)
                    }
                }
            }

            else -> {}
        }
    }
}