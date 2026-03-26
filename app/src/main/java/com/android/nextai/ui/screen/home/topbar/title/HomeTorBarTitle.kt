package com.android.nextai.ui.screen.home.topbar.title

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.icon.AppIcon


@Composable
fun HomeTorBarTitle(
    modifier: Modifier = Modifier, onShowDynamicWindow: () -> Unit = {}
) {
    TitleRow(
        text = "欢迎使用NextAI",
        icon = AppIcon.Sparkles,
        modifier = modifier.clickable {
            onShowDynamicWindow()
        })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TitleRow(
    modifier: Modifier = Modifier, text: String, icon: ImageVector
) {
    Box(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.height(Standard.ActionIconSize)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Standard.SpacingMd)
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
