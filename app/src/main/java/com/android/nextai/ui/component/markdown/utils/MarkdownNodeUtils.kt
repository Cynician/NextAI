package com.android.nextai.ui.component.markdown.utils

import android.util.Log
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.ext.math.MathExtension
import com.android.nextai.ui.component.markdown.ext.math.MathFormulaInLineNode
import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.ast.ThematicBreak
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableBody
import com.vladsch.flexmark.ext.tables.TableCaption
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.ext.tables.TableRow
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet


data class MarkdownParseResult(
    val nodes: List<MarkdownNode>,
    //Stable parsed character length
    val parsedLength: Int,
)

object MarkdownNodeUtils {
    private const val TAG = "MarkdownNodeUtils"
    private val parser by lazy {
        val options = MutableDataSet()
        options.set(
            Parser.EXTENSIONS,
            listOf(
                TablesExtension.create(),
                MathExtension.create()
            )
        )
        options.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
        Parser.builder(options).build()
    }

    fun parseChildren(node: Node): List<MarkdownNode> {
        val result = mutableListOf<MarkdownNode>()
        var child = node.firstChild
        while (child != null) {
            parseNode(child)?.let { result.add(it) }
            child = child.next
        }
        return result
    }

    fun parseNode(node: Node): MarkdownNode? {
        return when (node) {
            // Block
            is Paragraph -> MarkdownNode.Paragraph(parseChildren(node))
            is Heading -> MarkdownNode.Heading(level = node.level, children = parseChildren(node))
            is BulletList -> MarkdownNode.ListBlock(
                children = parseChildren(node),
                ordered = false,
                depth = 0
            )

            is OrderedList -> MarkdownNode.ListBlock(
                children = parseChildren(node),
                ordered = true,
                depth = 0
            )

            is ListItem -> MarkdownNode.ListItem(
                parseChildren(node),
                depth = 0,
                index = 0,
                ordered = false
            )

            is FencedCodeBlock -> MarkdownNode.FencedCodeBlock(
                code = node.contentChars.toString(),
                lang = node.info.toString()
            )

            is BlockQuote -> MarkdownNode.BlockQuote(parseChildren(node), depth = 0)

            // Inline
            is Text -> MarkdownNode.Text(node.chars.toString())
            is Emphasis -> MarkdownNode.Emphasis(parseChildren(node))
            is StrongEmphasis -> MarkdownNode.Strong(parseChildren(node))
            is Code -> MarkdownNode.InlineCode(node.text.toString())
            is Link -> MarkdownNode.Link(url = node.url.toString(), children = parseChildren(node))
            is ThematicBreak -> MarkdownNode.ThematicBreak


            // Table
            is TableBlock -> MarkdownNode.TableBlock(children = parseChildren(node))
            is TableBody -> MarkdownNode.TableBody(children = parseChildren(node))
            is TableCaption -> MarkdownNode.TableCaption(children = parseChildren(node))
            is TableHead -> MarkdownNode.TableHead(children = parseChildren(node))
            is TableRow -> MarkdownNode.TableRow(children = parseChildren(node))
            is TableCell -> MarkdownNode.TableCell(
                children = parseChildren(node),
                node.alignment,
                node.isHeader
            )

            is MathFormulaInLineNode -> MarkdownNode.InlineMath(node.text.toString())

            else -> {
                null
            }
        }
    }


    fun replaceLatexDelimiters(md: String): String {
        var processed = md

        // 1. 统一语法糖：将所有 \(, \), \[, \] 统一替换为 $ 和 $$
        processed = processed.replace("\\[", "$$").replace("\\]", "$$")
        processed = processed.replace("\\(", "$").replace("\\)", "$")

        // 2. 核心转义：匹配 $$ ... $$ 块级公式并转义内部的 |
        val blockDollarRegex = """\$\$([\s\S]*?)\$\$""".toRegex()
        processed = blockDollarRegex.replace(processed) { matchResult ->
            val content = matchResult.groupValues[1]
            "\$\${${content.replace("|", "\\|")}}\$\$"
        }

        // 3. 核心转义：匹配 $ ... $ 行内公式并转义内部的 |
        // [^$\n]+? 确保非贪婪匹配，且不跨行，精准锁定单行公式
        val inlineDollarRegex = """\$(?!\s)([^$\n]+?)(?<!\s)\$""".toRegex()
        processed = inlineDollarRegex.replace(processed) { matchResult ->
            val content = matchResult.groupValues[1]
            "\$${content.replace("|", "\\|")}\$"
        }

        return processed
    }

    private fun dump(node: Node, depth: Int = 0) {
        Log.d(
            "AST",
            "${" ".repeat(depth*2)}${node.javaClass.name} -> [${node.chars}]"
        )
        var child = node.firstChild
        while (child != null) {
            dump(child, depth + 2)
            child = child.next
        }
    }

    fun parseMarkDown(md: String): MarkdownParseResult {
        try {
            Log.d(TAG, "markdown = $md")

            val standardizedMd = replaceLatexDelimiters(md)
            Log.d(TAG, "markdown = $standardizedMd")
            val document = parser.parse(standardizedMd)


//            val document = parser.parse(md)

            Log.d(TAG, "firstChild = ${document.firstChild?.javaClass?.name}")
            dump(document, 0)


            val result = mutableListOf<MarkdownNode>()
            var node = document.firstChild

            while (node != null) {
                parseNode(node)?.let { result.add(it) }
                node = node.next
            }
            /** When streaming,  The last node may not be closed yet**/
            val lastNode = document.lastChild
            val parsedLength = if (lastNode != null && result.isNotEmpty()) {
                lastNode.startOffset
            } else {
                md.length
            }
            return MarkdownParseResult(nodes = result, parsedLength = parsedLength)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "parseMarkDown# failed",
                e
            )
            return MarkdownParseResult(
                nodes = listOf(MarkdownNode.Text(md)),
                parsedLength = md.length
            )
        }
    }
}

