package com.android.nextai.ui.screen.home.body.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.MarkdownNode
import com.android.nextai.ui.component.markdown.parser.MarkdownIncrementalParser
import com.android.nextai.ui.icon.HomeIcon

@Composable
internal fun AssistantBubbleView(
    parser: MarkdownIncrementalParser,
    messageId: Long = -1L,
    sessionId: Long = -1L,
    isGenerating: Boolean = false,
    isLastMessage: Boolean = false,
    onDelete: ((Long, Long) -> Unit)? = null,
    onRetry: ((Long, Long) -> Unit)? = null,
) {

    val context = LocalContext.current

    Column {
        SelectionContainer {
            Column {
                parser.stableNodes.forEach { node ->
                    AssistantBlockItem(node)
                }

                parser.pendingNodes.forEach { node ->
                    AssistantBlockItem(node)
                }
            }
        }

        if((isLastMessage && isGenerating ) || messageId == -1L) return@Column

        // Toolbar row at the bottom of each assistant bubble
        Row(
            modifier = Modifier
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val iconSize = 24.dp
            val buttonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ActionButton(
                modifier = Modifier.size(iconSize),
                icon = HomeIcon.Delete,
                contentDescription = "Delete",
                colors = buttonColors,
                onClickListener = {
                    if(isGenerating) return@ActionButton
                     onDelete?.invoke(messageId, sessionId)
                }
            )

            ActionButton(
                modifier = Modifier.size(iconSize),
                icon = HomeIcon.Copy,
                contentDescription = "Copy",
                colors = buttonColors,
                onClickListener = {
                    if(isGenerating) return@ActionButton
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text = parser.getRawContent()
                    cm.setPrimaryClip(ClipData.newPlainText("assistant", text))
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }
            )

            ActionButton(
                modifier = Modifier.size(iconSize),
                icon = HomeIcon.Refresh,
                contentDescription = "Retry",
                colors = buttonColors,
                onClickListener = {
                    if(isGenerating) return@ActionButton
                    onRetry?.invoke(messageId, sessionId)
                }
            )
        }
    }
}

@Composable
internal fun AssistantBlockItem(
    node: MarkdownNode
) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
    ) {
        MarkdownNodeView(node)
    }
}