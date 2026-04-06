package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.MarkdownNodeView
import com.android.nextai.ui.component.markdown.entity.MarkdownNode

@Composable
fun BlockQuoteView(node: MarkdownNode.BlockQuote, depth:Int = 0){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(((depth+1)*10).dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(Standard.SpacingSm),
        horizontalArrangement = Arrangement.spacedBy(Standard.SpacingSm)
    ){
        // left side vertical line
        Box(
            modifier = Modifier.width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier.padding(start = (depth * 8).dp)
        ){
            node.children.forEach { child->
                when(child){
                    is MarkdownNode.BlockQuote -> BlockQuoteView(child, depth + 1)
                    else -> MarkdownNodeView(child)
                }
            }
        }

    }
}