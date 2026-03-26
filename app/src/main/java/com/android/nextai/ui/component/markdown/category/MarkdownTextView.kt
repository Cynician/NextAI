package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.MarkdownElementView
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownElement

/**
 * Full markdown renderer for completed (non-streaming) messages.
 * renders each markdown element with appropriate styling.
 */
@Composable
fun MarkdownTextView(element: MarkdownElement?, modifier: Modifier = Modifier) {
    val parsedContent by remember { mutableStateOf(element) }
    val colors = InlineColors(
        codeBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        highlightBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        mathColor = MaterialTheme.colorScheme.primary
    )
    Column(
        modifier = modifier.padding(horizontal = Standard.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(Standard.SpacingXs)
    ) {
        MarkdownElementView(parsedContent, colors)
    }
}