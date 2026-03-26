package com.android.nextai.ui.screen.home.body

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.MarkdownElementView
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.component.markdown.entity.bottomSpacing
import com.android.nextai.ui.component.markdown.entity.topSpacing

@Composable
internal fun AssistantMessageBubble(element: MarkdownElement) {
    val element = element
    val scheme = MaterialTheme.colorScheme
    val colors = remember(scheme) {
        InlineColors(
            codeBg = scheme.surfaceVariant.copy(alpha = 0.5f),
            highlightBg = scheme.primary.copy(alpha = 0.3f),
            mathColor = scheme.primary
        )
    }
    SelectionContainer() {
        Box(
            modifier = Modifier
                .padding(
                    top = element.topSpacing(),
                    bottom = element.bottomSpacing()
                )
                .padding(horizontal = Standard.SpacingMd)
        ) {
            MarkdownElementView(element, colors)
        }
    }

}