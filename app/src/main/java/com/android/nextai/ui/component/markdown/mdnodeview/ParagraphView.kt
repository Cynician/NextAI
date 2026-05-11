package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode

@Composable
fun ParagraphView(node: MarkdownNode.Paragraph, colors: InlineColors) {
    val annotated = remember(node) {
        buildAnnotatedString { appendInlineNodes(node.children, colors = colors) }
    }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit){
                detectTapGestures { offsetPosition ->
                    layoutResult?.let { layout ->
                        val offset = layout.getOffsetForPosition(offsetPosition)
                        annotated.getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let { annotation->println("点击链接：${annotation.item}") }
                    }
                }
            },
        onTextLayout = {layoutResult = it }
    )
}