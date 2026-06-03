package com.android.nextai.ui.component.markdown.utils

import android.util.Log
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
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
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript
import com.vladsch.flexmark.ext.superscript.Superscript
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableBody
import com.vladsch.flexmark.ext.tables.TableCaption
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableHead
import com.vladsch.flexmark.ext.tables.TableRow
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node


data class MarkdownParseResult(
    val nodes: List<MarkdownNode>,
    //Stable parsed character length
    val parsedLength: Int
)

object MarkdownNodeUtils {
    private const val TAG = "MarkdownNodeUtils"
    private val parser by lazy {
        // TODO In the release version of the apk, using the extension below feature will
        //  cause an error and needs to be resolved
        Parser.builder()
            .extensions(
                listOf(
//                  SubscriptExtension.create(),
//                   SuperscriptExtension.create(),
//                    TablesExtension.create()
                )
            )
            .build()
    }

    fun parseChildren(node: Node): List<MarkdownNode>{
        val result = mutableListOf<MarkdownNode>()
        var child = node.firstChild
        while(child!=null){
            parseNode(child)?.let { result.add(it) }
            child = child.next
        }
        return result
    }

    fun parseNode(node:Node):MarkdownNode?{
        return when(node){
            // Block
            is Paragraph -> MarkdownNode.Paragraph(parseChildren(node))
            is Heading -> MarkdownNode.Heading(level = node.level, children = parseChildren(node))
            is BulletList -> MarkdownNode.ListBlock(children = parseChildren(node), ordered = false, depth = 0)
            is OrderedList -> MarkdownNode.ListBlock(children = parseChildren(node), ordered = true, depth = 0)
            is ListItem -> MarkdownNode.ListItem(parseChildren(node), depth = 0, index = 0, ordered = false)
            is FencedCodeBlock -> MarkdownNode.FencedCodeBlock(code = node.contentChars.toString(), lang = node.info.toString())
            is BlockQuote -> MarkdownNode.BlockQuote(parseChildren(node), depth = 0)
            // Inline
            is Text -> MarkdownNode.Text(node.chars.toString())
            is Emphasis -> MarkdownNode.Emphasis(parseChildren(node))
            is StrongEmphasis -> MarkdownNode.Strong(parseChildren(node))
            is Code -> MarkdownNode.InlineCode(node.text.toString())
            is Link -> MarkdownNode.Link(url = node.url.toString(), children = parseChildren(node))
            is ThematicBreak -> MarkdownNode.ThematicBreak
            is Strikethrough -> MarkdownNode.Strikethrough(children = parseChildren(node))
            is Subscript -> MarkdownNode.Subscript(children = parseChildren(node))
            is Superscript -> MarkdownNode.Superscript(children = parseChildren(node))
            // Table
            is TableBlock -> MarkdownNode.TableBlock(children = parseChildren(node))
            is TableBody -> MarkdownNode.TableBody(children = parseChildren(node))
            is TableCaption -> MarkdownNode.TableCaption(children = parseChildren(node))
            is TableHead -> MarkdownNode.TableHead(children = parseChildren(node))
            is TableRow -> MarkdownNode.TableRow(children = parseChildren(node))
            is TableCell -> MarkdownNode.TableCell(children = parseChildren(node), node.alignment, node.isHeader)
            else -> {null}
        }
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

