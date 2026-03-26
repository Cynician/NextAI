package com.android.nextai.ui.component.markdown.entity

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

/** Spacing above — headings get more to create visual section breaks. */
fun MarkdownElement.topSpacing(): Dp = when (this) {
        is MarkdownElement.Heading1 -> 14.dp
        is MarkdownElement.Heading2 -> 12.dp
        is MarkdownElement.Heading3 -> 10.dp
        is MarkdownElement.Heading4, is MarkdownElement.Heading5, is MarkdownElement.Heading6 -> 8.dp
        is MarkdownElement.CodeBlock, is MarkdownElement.Table, is MarkdownElement.MathBlock -> 6.dp
        is MarkdownElement.Quote -> 4.dp
        is MarkdownElement.Divider -> 8.dp
        is MarkdownElement.BulletPoint, is MarkdownElement.NumberedPoint -> 1.dp
        else -> 2.dp
    }

/** Spacing below — content elements get less so they group with following items. */
fun MarkdownElement.bottomSpacing(): Dp = when (this) {
    is MarkdownElement.Heading1, is MarkdownElement.Heading2, is MarkdownElement.Heading3 -> 3.dp
    is MarkdownElement.Heading4, is MarkdownElement.Heading5, is MarkdownElement.Heading6 -> 2.dp
    is MarkdownElement.CodeBlock, is MarkdownElement.Table, is MarkdownElement.MathBlock -> 6.dp
    is MarkdownElement.Quote -> 4.dp
    is MarkdownElement.Divider -> 8.dp
    is MarkdownElement.BulletPoint, is MarkdownElement.NumberedPoint -> 1.dp
    else -> 2.dp
}

// ── Sealed element model ──
@Serializable
sealed class MarkdownElement {
    data class Heading1(val text: String) : MarkdownElement()
    data class Heading2(val text: String) : MarkdownElement()
    data class Heading3(val text: String) : MarkdownElement()
    data class Heading4(val text: String) : MarkdownElement()
    data class Heading5(val text: String) : MarkdownElement()
    data class Heading6(val text: String) : MarkdownElement()
    data class Body(val text: String) : MarkdownElement()
    data class BulletPoint(val text: String, val level: Int = 0) : MarkdownElement()
    data class NumberedPoint(val text: String, val number: String) : MarkdownElement()
    data class Quote(val text: String, val level: Int = 1) : MarkdownElement()
    data class CodeBlock(val code: String, val language: String) : MarkdownElement()
    data class InlineCode(val text: String) : MarkdownElement()
    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>,
        val alignments: List<Alignment>,
    ) : MarkdownElement() {
        enum class Alignment { LEFT, CENTER, RIGHT }
    }

    data class MathBlock(val expression: String, val isTypst: Boolean = false) : MarkdownElement()
    data class InlineMath(val expression: String, val isTypst: Boolean = false) : MarkdownElement()
    data object Divider : MarkdownElement()
}
