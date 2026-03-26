package com.android.nextai.ui.component.markdown.category

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.entity.InlineColors

@Composable
fun BodyTextView(text: String, colors: InlineColors) {
    Text(
        text = InlineFormattingView(text, colors),
        style = MaterialTheme.typography.bodyMedium,
        color = LocalContentColor.current.copy(alpha = 0.87f),
        lineHeight = 20.sp
    )
}
