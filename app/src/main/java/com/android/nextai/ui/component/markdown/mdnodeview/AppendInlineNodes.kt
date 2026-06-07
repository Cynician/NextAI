package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.theme.MapleMonoFontFamily


fun AnnotatedString.Builder.appendInlineNodes(nodes: List<MarkdownNode>, colors: InlineColors) {

    nodes.forEach { node ->

        when (node) {
            is MarkdownNode.Text -> append(node.text)
            is MarkdownNode.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlineNodes(node.children, colors)
            }
            is MarkdownNode.Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlineNodes(node.children, colors)
            }
            is MarkdownNode.InlineCode -> withStyle(
                SpanStyle(
                    fontFamily = MapleMonoFontFamily,
                    background = colors.codeBg,
                    fontSize = 12.sp
                )
            ) {
                append(" ${node.code} ")
            }
            is MarkdownNode.Link -> {
                pushStringAnnotation(tag = "URL", annotation = node.url)
                withStyle(
                    SpanStyle(
                        color = colors.mathColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) { appendInlineNodes(node.children, colors) }
                pop()
            }
            is MarkdownNode.Strikethrough -> {
                pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                appendInlineNodes(node.children, colors)
                pop()
            }
            is MarkdownNode.Subscript -> {
                pushStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Subscript,
                        fontSize = 8.sp
                    )
                )
                appendInlineNodes(node.children, colors)
                pop()
            }
            is MarkdownNode.Superscript ->{
                pushStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Superscript,
                        fontSize = 8.sp
                    )
                )
                appendInlineNodes(node.children, colors)
                pop()
            }

            is MarkdownNode.InlineMath -> withStyle(
                SpanStyle(
                    color = colors.mathColor, // 你们主题色中的公式高亮色
                    fontStyle = FontStyle.Italic // 公式通常采用斜体
                )
            ) {
                // 如果你想保留 $ 符号显示：append("$${node.formula}$")
                // 如果只想干净地显示公式内部：
                append(node.formula)
            }
            else -> {}
        }
    }
}