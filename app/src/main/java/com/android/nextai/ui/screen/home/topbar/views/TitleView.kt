package com.android.nextai.ui.screen.home.topbar.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.icon.AppIcon


@Composable
fun TitleView(
    modifier: Modifier = Modifier, onShowDynamicWindow: () -> Unit = {}
) {
    TitleRow(
        text = "欢迎使用NextAI",
        icon = AppIcon.Sparkles,
        modifier = modifier,
        onClick = onShowDynamicWindow
    )
}

@Composable
fun TitleRow(
    modifier: Modifier = Modifier,
    text: String, icon: ImageVector,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(26.dp)
    Box(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.height(30.dp)
                .clip(shape = shape)
                .clickable(onClick = onClick),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
