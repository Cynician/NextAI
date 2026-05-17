package com.android.nextai.ui.screen.home.drawer.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.icon.AppIcon

@Composable
fun BatchActionView(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onCancel: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isSelectionMode,
        enter = slideInVertically(
            initialOffsetY = { it / 2 }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 }
        ) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ActionItem(
                    icon = AppIcon.Delete,
                    text = "删除",
                    tint = Color(0xFFCC4B4B),
                    onClick = onDelete
                )
                ActionItem(
                    icon = AppIcon.PushPin,
                    text = "置顶",
                    onClick = onPin
                )
                ActionItem(
                    icon = AppIcon.Cancel,
                    text = "取消",
                    onClick = onCancel
                )
            }
        }
    }
}

@Composable
fun ActionItem(
    icon: ImageVector?,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.labelLarge
        )
    }
}