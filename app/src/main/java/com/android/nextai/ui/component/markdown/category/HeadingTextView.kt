package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.android.nextai.ui.component.markdown.entity.InlineColors

@Composable
fun HeadingTextView(
    text: String, colors: InlineColors,
    fontSize: TextUnit, fontWeight: FontWeight,
    verticalPad: Dp, alpha: Float = 1f,
) {
    Text(
        text = InlineFormattingView(text, colors),
        style = MaterialTheme.typography.titleLarge,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = LocalContentColor.current.copy(alpha = alpha),
        modifier = Modifier.padding(vertical = verticalPad)
    )
}