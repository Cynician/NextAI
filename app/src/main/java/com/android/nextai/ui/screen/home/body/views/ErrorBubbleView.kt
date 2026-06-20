package com.android.nextai.ui.screen.home.body.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.views.TextView

@Composable
internal fun ErrorBubbleView(
    content: String,
) {

    SelectionContainer {

        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {

            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
            ) {
                TextView(MarkdownNode.Text(content))
            }
        }
    }
}