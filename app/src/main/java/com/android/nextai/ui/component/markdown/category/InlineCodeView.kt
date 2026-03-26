package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.theme.MapleMonoFontFamily

@Composable
fun InlineCodeView(text: String) {
    Text(
        text = text,
        fontFamily = MapleMonoFontFamily,
        fontSize = 12.sp,
        color = LocalContentColor.current.copy(alpha = 0.85f),
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}