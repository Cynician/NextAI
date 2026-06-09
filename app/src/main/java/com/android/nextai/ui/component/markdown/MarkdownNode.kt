package com.android.nextai.ui.component.markdown

import kotlinx.serialization.Serializable

@Serializable
sealed class MarkdownNode {
    // Block
    data class Paragraph(val children: List<MarkdownNode>) : MarkdownNode()
    data class Heading(val level: Int, val children: List<MarkdownNode>) : MarkdownNode()
    data class FencedCodeBlock(val code: String, val lang: String) : MarkdownNode()
    data class BlockQuote(val children: List<MarkdownNode>, val depth: Int = 0) : MarkdownNode()
    data class ListBlock(val children: List<MarkdownNode>, val ordered: Boolean, val depth: Int) :
        MarkdownNode()

    data class ListItem(
        val children: List<MarkdownNode>,
        val depth: Int = 0,
        val index: Int = 0,
        val ordered: Boolean,
    ) : MarkdownNode()


    // inline
    data class Text(val text: String) : MarkdownNode()
    data class Strong(val children: List<MarkdownNode>) : MarkdownNode()
    data class Emphasis(val children: List<MarkdownNode>) : MarkdownNode()
    data class InlineCode(val code: String) : MarkdownNode()
    data class Link(val url: String, val children: List<MarkdownNode>) : MarkdownNode()
    data object ThematicBreak : MarkdownNode()
    data class InlineMath(val formula: String) : MarkdownNode()    // latex

    // table
    data class TableBlock(val children: List<MarkdownNode>) : MarkdownNode()
    data class TableHead(val children: List<MarkdownNode>) : MarkdownNode()
    data class TableCaption(val children: List<MarkdownNode>) : MarkdownNode()
    data class TableBody(val children: List<MarkdownNode>) : MarkdownNode()
    data class TableRow(val children: List<MarkdownNode>) : MarkdownNode()
    data class TableCell(
        val children: List<MarkdownNode>,
        val alignment: com.vladsch.flexmark.ext.tables.TableCell.Alignment?,
        val header: Boolean,
    ) : MarkdownNode()
}