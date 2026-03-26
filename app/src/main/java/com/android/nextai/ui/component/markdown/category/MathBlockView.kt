package com.android.nextai.ui.component.markdown.category

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.component.markdown.utils.renderMathToUnicode
import com.android.nextai.ui.theme.MapleMonoFontFamily

@Composable
fun MathBlockView(expression: String, isTypst: Boolean) {
    val renderedMath = remember(expression) { renderMathToUnicode(expression) }
    val mathColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2211",
                fontFamily = MapleMonoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = mathColor
            )
            Text(
                text = if (isTypst) "TYPST" else "MATH",
                fontFamily = MapleMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                color = LocalContentColor.current.copy(alpha = 0.5f)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = renderedMath,
                fontFamily = MapleMonoFontFamily, fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = LocalContentColor.current, lineHeight = 24.sp
            )
        }
    }
}
