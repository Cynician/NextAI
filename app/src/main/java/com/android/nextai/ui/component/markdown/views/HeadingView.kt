package com.android.nextai.ui.component.markdown.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.appendInlineNodes
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.LatexRenderParams
import com.hrm.latex.renderer.measure.rememberLatexMeasurer

@Composable
fun HeadingView(node: MarkdownNode.Heading, colors: InlineColors, style: TextStyle) {
    val textStyle = when (node.level) {
        1 -> TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        2 -> TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        3 -> TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        else -> TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )
    }
    val inlineContentMap = remember(node.children) { mutableStateMapOf<String, InlineTextContent>() }
    val latexRenderParams = LatexRenderParams(
        latexMeasurer = rememberLatexMeasurer(),
        inlineContentMap = inlineContentMap,
    )
    val density = LocalDensity.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)) {
        ProvideTextStyle(textStyle) {
            Text(
                text = buildAnnotatedString {
                    appendInlineNodes(
                        nodes = node.children,
                        colors = colors,
                        style = style,
                        density = density,
                        latexRenderParams = latexRenderParams
                    )
                },
                inlineContent = inlineContentMap,
                style = MaterialTheme.typography.titleLarge,
                fontSize = textStyle.fontSize,
                fontWeight = textStyle.fontWeight,
                color = textStyle.color,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}