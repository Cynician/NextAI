package com.android.nextai.ui.component.markdown.parser

import android.util.Log
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.parser.ext.math.MathExtension
import com.android.nextai.ui.component.markdown.parser.ext.math.node.MathFormulaNode
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

object MarkdownParser {
    private const val TAG = "MarkdownParser"
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
//        Log.d(TAG, "${node.nodeName}:${node.chars}")
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
            is MathFormulaNode -> MarkdownNode.MathFormula(
                formula = node.text.toString(),
                isDisplayMode = node.isDisplayMode
            )

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

            else -> {
                null
            }
        }
    }

    /**
     * Process with original Markdown:
     *
     * 1. In some model outputs, mathematical formulas are replaced with \(...\) instead of $...$
     * and \[...\] instead of $$...$$, which does not comply with the LaTex specification and
     * therefore requires replacement.
     *
     * 2. The determination format for the Table column is |..|, which conflicts with absolute value
     * symbols in mathematical formulas, so escape processing is required.
     *
     * 3. Some llm model models often add line breaks internally when outputting mathematical
     * formulas for the \[...\] wrap, which makes parsing complicated because \n is the identifier
     * for paragraph node parsing. To facilitate parsing, after replacing \[and \] with $$,
     * unnecessary newlines or spaces must be removed from the contents of the envelope in $$...$$.
     */
    fun replaceLatexDelimiters(md: String): String {
        var processed = md

        // 1. Limitation: Only pairs of \[ ... \] and \( ... \) are replaced.
        val blockSlashRegex = """\\\[([\s\S]*?)\\]""".toRegex()
        processed = blockSlashRegex.replace(processed) { matchResult ->
            val content = matchResult.groupValues[1]
            "$$\n$content\n$$" // 替换为成对的 $$
        }

        val inlineSlashRegex = """\\\(([\s\S]*?)\\\)""".toRegex()
        processed = inlineSlashRegex.replace(processed) { matchResult ->
            val content = matchResult.groupValues[1]
            "$$content$"
        }

        // 2. Core modification: Match $$ ... $$ and remove blank characters before and after the internal text.
        val blockDollarRegex = """\$\$([\s\S]*?)\$\$""".toRegex()
        processed = blockDollarRegex.replace(processed) { matchResult ->
            val content = matchResult.groupValues[1].trim()
            "$$${content.replace("|", "\\|")}$$"
        }

        // 3. Core escaping: matches the $... $ inline formula and escapes the internal |.
        val inlineDollarRegex = """\$(?!\s)([^$\n]+?)(?<!\s)\$""".toRegex()
        processed = inlineDollarRegex.replace(processed) { matchResult ->
            val content = matchResult.groupValues[1]
            "$${content.replace("|", "\\|")}$"
        }

        return processed
    }

    fun parseMarkDown(md: String): MarkdownParseResult {
        try {

            val document = parser.parse(md)

            val result = mutableListOf<MarkdownNode>()
            var node = document.firstChild

            while (node != null) {
                parseNode(node)?.let { result.add(it) }
                node = node.next
            }

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

