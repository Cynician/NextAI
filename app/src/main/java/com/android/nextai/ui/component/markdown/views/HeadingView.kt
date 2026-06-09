package com.android.nextai.ui.component.markdown.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val density = LocalDensity.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)) {
        ProvideTextStyle(textStyle) {
            Text(
                text = buildAnnotatedString {
                    appendInlineNodes(node.children, colors = colors, style, density = density,)
                },
                style = MaterialTheme.typography.titleLarge,
                fontSize = textStyle.fontSize,
                fontWeight = textStyle.fontWeight,
                color = textStyle.color,
                modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
            )
        }
    }
}