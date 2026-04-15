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
                append(node.code)
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
            else -> {}
        }
    }
}