package com.android.nextai.ui.component.markdown.views

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.appendInlineNodes
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.LatexRenderParams
import com.hrm.latex.renderer.measure.rememberLatexMeasurer

@Composable
fun ParagraphView(node: MarkdownNode.Paragraph, colors: InlineColors, style: TextStyle) {
    val density = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    val latexMeasurer = rememberLatexMeasurer()

    // 1. Using Column wrappers allows internal text blocks to be arranged vertically with formula blocks.
    Column(modifier = Modifier.fillMaxWidth()) {

        // Temporary storage of consecutive inline nodes, and packaging and rendering once display mode formulas are encountered.
        val currentInlineChunk = remember(node.children) { mutableListOf<MarkdownNode>() }

        // Define a closure for a common rendering of a common rich text block.
        @Composable
        fun RenderInlineChunk(inlineNodes: List<MarkdownNode>) {
            if (inlineNodes.isEmpty()) return

            val inlineContentMap = remember(inlineNodes) { mutableStateMapOf<String, InlineTextContent>() }
            val latexRenderParams = LatexRenderParams(
                latexMeasurer = latexMeasurer,
                inlineContentMap = inlineContentMap,
            )

            val annotatedString = remember(inlineNodes) {
                buildAnnotatedString {
                    appendInlineNodes(
                        nodes = inlineNodes,
                        colors = colors,
                        style = style,
                        density = density,
                        latexRenderParams = latexRenderParams
                    )
                }
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

            Text(
                text = annotatedString,
                inlineContent = inlineContentMap,
                style = style,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .pointerInput(inlineNodes) {
                        detectTapGestures { offsetPosition ->
                            layoutResult?.let { layout ->
                                val offset = layout.getOffsetForPosition(offsetPosition)
                                annotatedString.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        println("Click the link：${annotation.item}")
                                        try {
                                            uriHandler.openUri(annotation.item)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                            }
                        }
                    },
                onTextLayout = { layoutResult = it }
            )
        }

        // 2. Core splitting logic: traverses all child nodes.
        node.children.forEach { child ->

            if (child is MarkdownNode.MathFormula && child.isDisplayMode) {

                // When encountering display mode formulas, first render the accumulated inline text
                // stream from the previous one.
                if (currentInlineChunk.isNotEmpty()) {
                    RenderInlineChunk(inlineNodes = currentInlineChunk.toList())
                    currentInlineChunk.clear()
                }

                // Uniformly clean and format the DisplayMode formula prefix/suffix.
                val latexFormula = child.formula.replace("\\|", "|").let { raw ->
                    val clean = raw.trim().removePrefix("$$").removeSuffix("$$").trim()
                    "$$$clean$$"
                }

                // Render independently into block-level Composable (no longer stuffed into text as inlineContent).
                MathBlockView(
                    formula = latexFormula,
                    style = style
                )

            } else {
                // Collect ordinary inline nodes.
                currentInlineChunk.add(child)
            }
        }

        // 3. After the loop ends, the last batch of legacy inline nodes is rendered.
        if (currentInlineChunk.isNotEmpty()) {
            RenderInlineChunk(inlineNodes = currentInlineChunk.toList())
        }
    }
}