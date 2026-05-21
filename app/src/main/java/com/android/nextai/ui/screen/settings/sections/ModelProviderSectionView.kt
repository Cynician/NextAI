package com.android.nextai.ui.screen.settings.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.SettingButton
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.component.other.SuccessTipLabel
import com.android.nextai.ui.icon.SettingsIcon


data class ModelProvider(
    val name: String,
    val desc: String,
    val icon: ImageVector? = SettingsIcon.Settings,
    val isConfig: Boolean = false,
)

@Composable
fun ModelProviderSectionView(
    onQwenClick: () -> Unit,
) {
    val customProviders = listOf(
        ModelProvider(
            name = "自定义提供商",
            desc = "添加自定义 API 接口与模型",
            icon = SettingsIcon.Settings,
            isConfig = true
        ),
    )

    SectionHeader(title = "模型提供商")

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProviderItem(
            provider = ModelProvider(
                name = "通义千问",
                desc = "阿里巴巴通义大模型系列",
                icon = SettingsIcon.Qianwen
            ),
            onClick = onQwenClick
        )

        customProviders.forEach { provider ->
            ProviderItem(
                provider = provider,
                onClick = {}
            )
        }

        SettingButton(
            text = "添加更多提供商",
            icon = SettingsIcon.Add,
            onClick = {}
        )
    }
}

@Composable
fun ProviderItem(
    provider: ModelProvider,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(12.dp))
            .clickable { onClick() },
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = provider.icon!!,
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (provider.isConfig) {

                        Spacer(modifier = Modifier.width(8.dp))

                        SuccessTipLabel(
                            visible = true,
                            text = "已配置"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = provider.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = SettingsIcon.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}