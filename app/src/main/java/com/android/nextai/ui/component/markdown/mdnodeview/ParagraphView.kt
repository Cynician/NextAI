package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.hrm.latex.renderer.measure.rememberLatexMeasurer

@Composable
fun ParagraphView(node: MarkdownNode.Paragraph, colors: InlineColors, style: TextStyle) {
    val latexMeasurer = rememberLatexMeasurer()
    val inlineContentMap = remember(node.children) { mutableStateMapOf<String, InlineTextContent>() }
    val density = LocalDensity.current
    val annotatedString = remember(node.children) {
        buildAnnotatedString {
            appendInlineNodes(
                nodes = node.children,
                colors = colors,
                style = style,
                density = density,
                latexMeasurer = latexMeasurer, // 👈 2. 顺着这里传进去
                inlineContentMap = inlineContentMap,
            )
        }
    }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    /** Obtain the current UriHandler for calling the system browser **/
    val uriHandler = LocalUriHandler.current

    Text(
        text = annotatedString,
        inlineContent = inlineContentMap,
        style = style,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit){
                detectTapGestures { offsetPosition ->
                    layoutResult?.let { layout ->
                        val offset = layout.getOffsetForPosition(offsetPosition)
                        annotatedString.getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let {
                                annotation->println("Click the link：${annotation.item}")
                                try {
                                    uriHandler.openUri(annotation.item)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    }
                }
            },
        onTextLayout = {layoutResult = it }
    )
}