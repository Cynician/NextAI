package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.theme.MapleMonoFontFamily
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.LatexAutoWrap
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.latex.renderer.model.LatexConfig


fun AnnotatedString.Builder.appendInlineNodes(
    nodes: List<MarkdownNode>,
    colors: InlineColors,
    style: TextStyle,
    density: Density,
    latexMeasurer: LatexMeasurerState? = null, // 👈 1. 核心：通过参数将同步测量器注入进来
    inlineContentMap: MutableMap<String, InlineTextContent>? = null,
    maxContentWidth: Dp = 0.dp // 接收限制上限
) {

    nodes.forEachIndexed { index, node ->

        when (node) {
            is MarkdownNode.Text -> append(node.text)
            is MarkdownNode.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlineNodes(node.children, colors, style = style, density = density)
            }

            is MarkdownNode.Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlineNodes(node.children, colors, style=style, density =  density)
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
                ) { appendInlineNodes(node.children, colors, style, density = density) }
                pop()
            }

            is MarkdownNode.Subscript -> {
                pushStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Subscript, fontSize = 8.sp
                    )
                )
                appendInlineNodes(node.children, colors, style, density = density)
                pop()
            }

            is MarkdownNode.Superscript -> {
                pushStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Superscript, fontSize = 8.sp
                    )
                )
                appendInlineNodes(node.children, colors, style, density = density)
                pop()
            }

            is MarkdownNode.InlineMath -> {
                val formula = try {
                    val field = node.javaClass.getDeclaredField("formula")
                    field.isAccessible = true
                    field.get(node) as String
                } catch (e: Exception) { node.toString() }

                // 统一公式格式（清洗前后缀）
                val cleanedFormula = formula.replace("\\|", "|").let {
                    if (!it.startsWith("$")) "$$it$" else it
                }

                val id = "math_${node.hashCode()}_$index"

                if (inlineContentMap != null && !inlineContentMap.containsKey(id) && latexMeasurer!=null) {
                    // 2. ⚡ 核心威力：利用现成的测量器，在首帧直接进行纯同步内存测量！
                    val dimensions = latexMeasurer.measure(
                        latex = cleanedFormula,
                        config = LatexConfig(
                            fontSize = style.fontSize,
                            // 这里可以带上颜色，防止有些特殊符号（如根号、分数线）测量受颜色配置影响
                        )
                    )

                    if (dimensions != null) {
                        with(density) {
                            // 3. 将同步拿到的像素大小（包含 Padding） 1:1 转化为 SP
                            val rawWidthDp = dimensions.widthPx.toDp()
                            val exactHeightSp = dimensions.heightPx.toSp()
                            val isTooLong = rawWidthDp > maxContentWidth
                            val finalPlaceholderWidth = if (isTooLong && maxContentWidth > 0.dp) maxContentWidth else rawWidthDp
                            inlineContentMap[id] = InlineTextContent(
                                Placeholder(
                                    width = finalPlaceholderWidth.toSp(),
                                    height = exactHeightSp,
                                    // 💡 重点：既然外壳尺寸已经 100% 锁死，这里对齐方式用 Center 或 TextBottom 都会非常稳
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                )
                            ) {
                                // 4. 关键：外层的 Placeholder 容器大小在首帧已经固定。
                                // 内部包裹原生的 Latex 组件。即使它内部有异步延迟，也只是在固定好的框子里“闪”一下，
                                // 绝对无法撑爆外层、也无法导致外层布局二次刷新导致乱飞！
                                Box(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .let { modifier ->
                                            if (isTooLong) {
                                                modifier.horizontalScroll(rememberScrollState())
                                            } else {
                                                modifier
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Latex(
                                        latex = cleanedFormula,
                                        config = LatexConfig(
                                            fontSize = style.fontSize,
                                            // 保持库原生的配置传入
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // 防御性降级：万一测量失败，给一个兜底的普通字符大小占位，防止崩溃
                        inlineContentMap[id] = InlineTextContent(
                            Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.Center)
                        ) {}
                    }
                }

                // 写入占位符
                appendInlineContent(id, "[math]")
            }

            else -> {}
        }

    }

}