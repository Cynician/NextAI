package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.entity.InlineColors

@Composable
fun NumberedPointView(text: String, number: String, colors: InlineColors) {
    Row(
        modifier = Modifier.padding(start = Standard.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp)
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