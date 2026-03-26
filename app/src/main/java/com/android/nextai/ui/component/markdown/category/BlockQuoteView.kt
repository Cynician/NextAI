package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.entity.InlineColors

@Composable
fun BlockQuoteView(text: String, level: Int, colors: InlineColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ((level - 1) * 10).dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(Standard.SpacingSm),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(18.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = InlineFormattingView(text, colors),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = 0.87f),
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}