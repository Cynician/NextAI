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
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.latex.renderer.measure.rememberLatexMeasurer

// Define a sealed class to represent plain text blocks or formula block after slicing.
private sealed interface ParagraphChunk {
    data class InlineChunk(val nodes: List<MarkdownNode>) : ParagraphChunk
    data class DisplayMathChunk(val formula: String) : ParagraphChunk
}

@Composable
fun ParagraphView(node: MarkdownNode.Paragraph, colors: InlineColors, style: TextStyle) {
    val density = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    val latexMeasurer = rememberLatexMeasurer()

    val chunks = remember(node.children) {
        val result = mutableListOf<ParagraphChunk>()
        val currentInline = mutableListOf<MarkdownNode>()

        node.children.forEach { child ->
            if (child is MarkdownNode.MathFormula && child.isDisplayMode) {
                if (currentInline.isNotEmpty()) {
                    result.add(ParagraphChunk.InlineChunk(currentInline.toList()))
                    currentInline.clear()
                }
                val latexFormula = child.formula.replace("\\|", "|").let { raw ->
                    val clean = raw.trim().removePrefix("$$").removeSuffix("$$").trim()
                    "$$$clean$$"
                }
                result.add(ParagraphChunk.DisplayMathChunk(latexFormula))
            } else {
                currentInline.add(child)
            }
        }
        if (currentInline.isNotEmpty()) {
            result.add(ParagraphChunk.InlineChunk(currentInline.toList()))
        }
        result
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        chunks.forEach { chunk ->
            when (chunk) {
                is ParagraphChunk.InlineChunk -> {
                    RenderInlineChunk(
                        inlineNodes = chunk.nodes,
                        colors = colors,
                        style = style,
                        density = density,
                        uriHandler = uriHandler,
                        latexMeasurer = latexMeasurer
                    )
                }
                is ParagraphChunk.DisplayMathChunk -> {
                    MathBlockView(
                        formula = chunk.formula,
                        style = style
                    )
                }
            }
        }
    }
}

/**
 * Block Rendering
 */
@Composable
private fun RenderInlineChunk(
    inlineNodes: List<MarkdownNode>,
    colors: InlineColors,
    style: TextStyle,
    density: androidx.compose.ui.unit.Density,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    latexMeasurer: LatexMeasurerState
) {
    val inlineContentMap = remember(inlineNodes) { mutableStateMapOf<String, InlineTextContent>() }

    val latexRenderParams = remember(inlineNodes, latexMeasurer) {
        LatexRenderParams(
            latexMeasurer = latexMeasurer,
            inlineContentMap = inlineContentMap,
        )
    }

    val annotatedString = remember(inlineNodes, colors, style, density) {
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