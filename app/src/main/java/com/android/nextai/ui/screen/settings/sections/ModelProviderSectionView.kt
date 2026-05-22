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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.datastore.entity.ProviderType
import com.android.nextai.ui.component.button.SettingButton
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.component.other.SuccessTipLabel
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.viewmodel.provider.ProviderViewModel



@Composable
fun ModelProviderSectionView(
    providerViewModel: ProviderViewModel,
    onQwenClick: () -> Unit,
) {

    val providers by providerViewModel.providers.collectAsStateWithLifecycle(emptyList())

    SectionHeader(title = "模型提供商")
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        providers.forEach { provider ->
            val icon = when(provider.type){
                ProviderType.QWEN -> SettingsIcon.Qianwen
                else -> SettingsIcon.Settings
            }
            val onClick = when (provider.type) {
                ProviderType.QWEN ->  ({
                    onQwenClick()
                    providerViewModel.setCurrentProvider(provider.id)
                })
                else -> ({})
            }

            ProviderItem(
                provider = provider,
                icon = icon,
                onClick = onClick ,
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
    provider: ProviderEntity,
    icon: ImageVector = SettingsIcon.Settings,
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
                        imageVector = icon,
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

                    if (provider.isOK) {
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