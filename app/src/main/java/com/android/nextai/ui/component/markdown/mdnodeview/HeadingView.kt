package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownNode

@Composable
fun HeadingView(node: MarkdownNode.Heading, colors: InlineColors) {
    val textStyle = when (node.level) {
        1 -> TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        2 -> TextStyle(
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        3 -> TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        4 -> TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        5 -> TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        6 -> TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )

        else -> TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalContentColor.current.copy(alpha = 1f)
        )
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)) {
        ProvideTextStyle(textStyle) {
            Text(
                text = buildAnnotatedString { appendInlineNodes(node.children, colors = colors) },
                style = MaterialTheme.typography.titleLarge,
                fontSize = textStyle.fontSize,
                fontWeight = textStyle.fontWeight,
                color = textStyle.color,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}