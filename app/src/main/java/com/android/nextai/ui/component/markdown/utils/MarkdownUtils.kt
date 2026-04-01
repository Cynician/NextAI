package com.android.nextai.ui.component.markdown.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.theme.MapleMonoFontFamily

object MarkdownUtils {
    private val BULLET_REGEX = Regex("^\\s*[+\\-*]\\s+.+")
    private val NUMBERED_REGEX = Regex("^\\d+\\.\\s+.+")
    private val TABLE_SEP_REGEX = Regex("^:?-{1,}:?$")
    private val LATEX_BEGIN_REGEX = Regex("""\\{1,2}begin\s*\{(equation|align|gather|multline|displaymath|math)\*?\}""")
    private val LATEX_NORM_FIX = Regex("""\\begin\s+\{""")
    private val LATEX_ENV_REGEX = Regex("""\\begin\{(equation|align|gather|multline|displaymath|math)(\*?)\}""")

    fun parseText2Markdown(text: String):MarkdownElement{
        if(text.startsWith("######") && text.length>6){
            return MarkdownElement.Heading6(text.substring(6))
        }else if(text.startsWith("#####")&& text.length>5){
            return MarkdownElement.Heading5(text.substring(5))
        }else if(text.startsWith("####")&& text.length>4){
            return MarkdownElement.Heading4(text.substring(4))
        }else if(text.startsWith("###") && text.length>3){
            return MarkdownElement.Heading3(text.substring(3))
        }else if(text.startsWith("##")&& text.length>2){
            return MarkdownElement.Heading2(text.substring(2))
        }else if(text.startsWith("#")&& text.length>1){
            return MarkdownElement.Heading1(text.substring(1))
        }else if(text.matches(NUMBERED_REGEX)){
            return MarkdownElement.NumberedPoint(text.substringAfter(". "), text.substringBefore("."))
        }else if(text.matches(BULLET_REGEX)){
                val level = text.takeWhile { it == ' ' }.length
                return MarkdownElement.BulletPoint(text.trimStart().substring(1), level)
        } else if(text.startsWith(">")){
            val level = text.takeWhile { it == '>' }.length
            return MarkdownElement.Quote(text.substring(level).trim(), level)
        }
        else if(text.startsWith("```")){
            val lang = if (text.length > 3) text.substring(3) else ""
            val code = text.substring(3 + lang.length)
            return MarkdownElement.CodeBlock(code, lang)
        }else if(text.trim() == "---" || text.trim()=="___" || text.trim() == "***"){
            return MarkdownElement.Divider
        }
        return MarkdownElement.Body(text)
    }

    fun parseMarkdown(text: String): List<MarkdownElement> {
        val elements = mutableListOf<MarkdownElement>()
        val lines = text.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            when {
                // Block math: \[...\]
                line.trimStart().startsWith("\\[") || line.trimStart().startsWith("\\\\[") -> {
                    val isDouble = line.contains("\\\\[")
                    val startPat = if (isDouble) "\\\\[" else "\\["
                    val startCol = line.indexOf(startPat)
                    val after = line.substring(startCol + startPat.length)
                    val endSingle = "\\]"; val endDouble = "\\\\]"
                    val sameEnd = when {
                        after.contains(endDouble) -> after.indexOf(endDouble)
                        after.contains(endSingle) -> after.indexOf(endSingle)
                        else -> -1
                    }
                    if (sameEnd != -1) {
                        elements.add(MarkdownElement.MathBlock(after.substring(0, sameEnd).trim().replace("\\\\", "\\"), false))
                    } else {
                        val mathLines = mutableListOf<String>()
                        if (after.isNotBlank()) mathLines.add(after.replace("\\\\", "\\"))
                        i++
                        while (i < lines.size && !lines[i].contains(endSingle) && !lines[i].contains(endDouble)) {
                            mathLines.add(lines[i].replace("\\\\", "\\")); i++
                        }
                        if (i < lines.size) {
                            val cl = lines[i]
                            val ci = if (cl.contains(endDouble)) cl.indexOf(endDouble) else cl.indexOf(endSingle)
                            if (ci > 0) mathLines.add(cl.substring(0, ci).replace("\\\\", "\\"))
                        }
                        elements.add(MarkdownElement.MathBlock(mathLines.joinToString("\n").trim(), false))
                    }
                }

                // LaTeX math environments
                LATEX_BEGIN_REGEX.containsMatchIn(line) -> {
                    val norm = line.replace("\\\\", "\\").replace(LATEX_NORM_FIX, "\\begin{")
                    val envMatch = LATEX_ENV_REGEX.find(norm)
                    val envName = envMatch?.groupValues?.get(1) ?: "equation"
                    val starred = envMatch?.groupValues?.get(2) ?: ""
                    val endRx = Regex("""\\{1,2}end\s*\{${Regex.escape(envName)}${Regex.escape(starred)}\}""")
                    val mathLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !endRx.containsMatchIn(lines[i])) {
                        mathLines.add(lines[i].replace("\\\\", "\\")); i++
                    }
                    val expr = mathLines.joinToString("\n").trim()
                    if (expr.isNotBlank()) elements.add(MarkdownElement.MathBlock(expr, false))
                }

                // Block math: $$...$$
                line.trimStart().startsWith("$$") -> {
                    val startCol = line.indexOf("$$")
                    val after = line.substring(startCol + 2)
                    val sameEnd = after.indexOf("$$")
                    if (sameEnd != -1) {
                        val expr = after.substring(0, sameEnd).trim().replace("\\\\", "\\")
                        elements.add(MarkdownElement.MathBlock(expr, expr.contains("#")))
                    } else {
                        val mathLines = mutableListOf<String>()
                        if (after.isNotBlank()) mathLines.add(after.replace("\\\\", "\\"))
                        i++
                        while (i < lines.size && !lines[i].contains("$$")) {
                            mathLines.add(lines[i].replace("\\\\", "\\")); i++
                        }
                        val expr = mathLines.joinToString("\n").trim()
                        elements.add(MarkdownElement.MathBlock(expr, expr.contains("#")))
                    }
                }

                // Fenced code block
                line.startsWith("```") -> {
                    val language = line.removePrefix("```").trim()
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```")) { codeLines.add(lines[i]); i++ }
                    elements.add(MarkdownElement.CodeBlock(codeLines.joinToString("\n"), language))
                }

                // Indented code block
                line.trimStart().startsWith("    ") && line.trim().isNotEmpty() -> {
                    val codeLines = mutableListOf<String>()
                    while (i < lines.size && (lines[i].trimStart().startsWith("    ") || lines[i].isBlank())) {
                        if (lines[i].trim().isNotEmpty()) codeLines.add(lines[i].removePrefix("    "))
                        i++
                    }
                    i--
                    elements.add(MarkdownElement.CodeBlock(codeLines.joinToString("\n"), ""))
                }

                // Headings
                line.startsWith("###### ") -> elements.add(MarkdownElement.Heading6(line.removePrefix("###### ")))
                line.startsWith("##### ") -> elements.add(MarkdownElement.Heading5(line.removePrefix("##### ")))
                line.startsWith("#### ") -> elements.add(MarkdownElement.Heading4(line.removePrefix("#### ")))
                line.startsWith("### ") -> elements.add(MarkdownElement.Heading3(line.removePrefix("### ")))
                line.startsWith("## ") -> elements.add(MarkdownElement.Heading2(line.removePrefix("## ")))
                line.startsWith("# ") -> elements.add(MarkdownElement.Heading1(line.removePrefix("# ")))

                // Bullet points
                line.matches(BULLET_REGEX) -> {
                    val level = line.takeWhile { it == ' ' }.length / 2
                    elements.add(MarkdownElement.BulletPoint(line.trimStart().substring(2), level))
                }

                // Numbered lists
                line.matches(NUMBERED_REGEX) -> {
                    elements.add(MarkdownElement.NumberedPoint(line.substringAfter(". "), line.substringBefore(".")))
                }

                // Block quotes
                line.startsWith(">") -> {
                    val level = line.takeWhile { it == '>' }.length
                    elements.add(MarkdownElement.Quote(line.substring(level).trim(), level))
                }

                // Table
                line.startsWith("|") && i + 1 < lines.size && isTableSeparator(lines[i + 1]) -> {
                    val headers = parseTableRow(line)
                    i++ // skip to separator
                    val alignments = lines[i].split("|").filter { it.isNotBlank() }.map { cell ->
                        val t = cell.trim()
                        when {
                            t.startsWith(":") && t.endsWith(":") -> MarkdownElement.Table.Alignment.CENTER
                            t.endsWith(":") -> MarkdownElement.Table.Alignment.RIGHT
                            else -> MarkdownElement.Table.Alignment.LEFT
                        }
                    }
                    i++
                    val rows = mutableListOf<List<String>>()
                    while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                        val row = parseTableRow(lines[i])
                        val normalized = when {
                            row.size < headers.size -> row + List(headers.size - row.size) { "" }
                            row.size > headers.size -> row.take(headers.size)
                            else -> row
                        }
                        rows.add(normalized); i++
                    }
                    i--
                    val finalAlignments = when {
                        alignments.size < headers.size -> alignments + List(headers.size - alignments.size) { MarkdownElement.Table.Alignment.LEFT }
                        alignments.size > headers.size -> alignments.take(headers.size)
                        else -> alignments
                    }
                    elements.add(MarkdownElement.Table(headers, rows, finalAlignments))
                }

                // Divider
                line == "---" || line == "___" || line == "***" -> elements.add(MarkdownElement.Divider)

                // Body text
                line.isNotBlank() -> elements.add(MarkdownElement.Body(line))
            }
            i++
        }
        return elements
    }

    private fun isTableSeparator(line: String): Boolean {
        val trimmed = line.trim()
        if (!trimmed.contains("|")) return false
        val cells = trimmed.split("|").filter { it.isNotBlank() }
        return cells.isNotEmpty() && cells.all { it.trim().matches(TABLE_SEP_REGEX) }
    }

    private fun parseTableRow(line: String): List<String> =
        line.trim().removePrefix("|").removeSuffix("|").split("|").map { it.trim() }

// ── Inline formatting ──
    /**
     * Find closing ** for bold, accounting for *** (italic close + bold close).
     * When a run of 3+ stars is found, the last 2 close bold, any preceding ones close italic.
     */
    private fun findStarClose(text: String, from: Int): Int {
        var i = from
        while (i < text.length) {
            if (text[i] == '*') {
                var end = i
                while (end < text.length && text[end] == '*') end++
                if (end - i >= 2) return end - 2
                i = end
            } else i++
        }
        return -1
    }

    /** Pure function — no @Composable, no MaterialTheme reads. */
    internal fun buildInlineFormatted(text: String, colors: InlineColors): AnnotatedString = buildAnnotatedString {
        var i = 0
        val chars = text.toCharArray()
        while (i < chars.size) {
            when {
                // Bold+Italic ***...***
                i + 2 < chars.size && chars[i] == '*' && chars[i + 1] == '*' && chars[i + 2] == '*' -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(buildInlineFormatted(text.substring(i + 3, end), colors))
                        }; i = end + 3
                    } else { append(chars[i]); i++ }
                }
                // Bold **...**
                i + 1 < chars.size && chars[i] == '*' && chars[i + 1] == '*' -> {
                    val end = findStarClose(text, i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(buildInlineFormatted(text.substring(i + 2, end), colors))
                        }; i = end + 2
                    } else { append(chars[i]); i++ }
                }
                // Bold __...__
                i + 1 < chars.size && chars[i] == '_' && chars[i + 1] == '_' -> {
                    val end = text.indexOf("__", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(buildInlineFormatted(text.substring(i + 2, end), colors))
                        }; i = end + 2
                    } else { append(chars[i]); i++ }
                }
                // Italic *...*
                chars[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(buildInlineFormatted(text.substring(i + 1, end), colors))
                        }; i = end + 1
                    } else { append(chars[i]); i++ }
                }
                // Italic _..._
                chars[i] == '_' -> {
                    val end = text.indexOf('_', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(buildInlineFormatted(text.substring(i + 1, end), colors))
                        }; i = end + 1
                    } else { append(chars[i]); i++ }
                }
                // Strikethrough ~~...~~
                i + 1 < chars.size && chars[i] == '~' && chars[i + 1] == '~' -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(buildInlineFormatted(text.substring(i + 2, end), colors))
                        }; i = end + 2
                    } else { append(chars[i]); i++ }
                }
                // Inline code `...`
                chars[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontFamily = MapleMonoFontFamily, background = colors.codeBg, fontSize = 12.sp)) {
                            append(" ${text.substring(i + 1, end)} ")
                        }; i = end + 1
                    } else { append(chars[i]); i++ }
                }
                // Highlight ==...==
                i + 1 < chars.size && chars[i] == '=' && chars[i + 1] == '=' -> {
                    val end = text.indexOf("==", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(background = colors.highlightBg)) {
                            append(buildInlineFormatted(text.substring(i + 2, end), colors))
                        }; i = end + 2
                    } else { append(chars[i]); i++ }
                }
                // Inline math \(...\)
                i + 1 < chars.size && chars[i] == '\\' && chars[i + 1] == '(' -> {
                    val endIdx = text.indexOf("\\)", i + 2)
                    if (endIdx != -1) {
                        val rendered = renderMathToUnicode(text.substring(i + 2, endIdx))
                        withStyle(SpanStyle(fontFamily = MapleMonoFontFamily, fontStyle = FontStyle.Italic, color = colors.mathColor)) { append(rendered) }
                        i = endIdx + 2
                    } else { append(chars[i]); i++ }
                }
                // Inline math $...$
                chars[i] == '$' && (i + 1 >= chars.size || chars[i + 1] != '$') -> {
                    val end = text.indexOf('$', i + 1)
                    if (end != -1 && end > i + 1) {
                        val rendered = renderMathToUnicode(text.substring(i + 1, end))
                        withStyle(SpanStyle(fontFamily = MapleMonoFontFamily, fontStyle = FontStyle.Italic, color = colors.mathColor)) { append(rendered) }
                        i = end + 1
                    } else { append(chars[i]); i++ }
                }
                // Default — handle surrogates
                else -> {
                    val c = chars[i]
                    if (c.isHighSurrogate() && i + 1 < chars.size && chars[i + 1].isLowSurrogate()) {
                        append(c); append(chars[i + 1]); i += 2
                    } else { append(c); i++ }
                }
            }
        }
    }

    fun DrawScope.drawTableRow(
        measuredCells: List<TextLayoutResult>,
        colCount: Int, colWidth: Float, cellPadH: Float, cellPadV: Float,
        dividerWidth: Float, rowY: Float, rowHeight: Float,
        alignments: List<MarkdownElement.Table.Alignment>, outlineColor: Color
    ) {
        for (ci in 0 until colCount) {
            val cellX = ci * (colWidth + dividerWidth)
            if (ci > 0) drawRect(color = outlineColor.copy(alpha = 0.3f), topLeft = Offset(cellX - dividerWidth, rowY), size = Size(dividerWidth, rowHeight))
            if (ci < measuredCells.size) {
                val measured = measuredCells[ci]
                val align = alignments.getOrNull(ci) ?: MarkdownElement.Table.Alignment.LEFT
                val tw = measured.size.width.toFloat()
                val avail = colWidth - cellPadH * 2
                val ox = when (align) {
                    MarkdownElement.Table.Alignment.CENTER -> cellX + cellPadH + (avail - tw).coerceAtLeast(0f) / 2
                    MarkdownElement.Table.Alignment.RIGHT -> cellX + cellPadH + (avail - tw).coerceAtLeast(0f)
                    else -> cellX + cellPadH
                }
                drawText(textLayoutResult = measured, topLeft = Offset(ox, rowY + cellPadV))
            }
        }
    }

}