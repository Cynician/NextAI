package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.utils.renderMathToUnicode
import com.android.nextai.ui.theme.MapleMonoFontFamily

@Composable
fun InlineMathView(expression: String) {
    val renderedMath = remember(expression) { renderMathToUnicode(expression) }
    Text(
        text = renderedMath,
        fontFamily = MapleMonoFontFamily,
        fontSize = 14.sp,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}