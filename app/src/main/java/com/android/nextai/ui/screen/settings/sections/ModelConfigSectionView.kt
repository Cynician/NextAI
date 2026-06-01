package com.android.nextai.ui.screen.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.ui.component.button.ActionNavigateButton
import com.android.nextai.ui.component.other.SectionHeader

@Composable
fun ModelConfigSectionView(
    sectionTitle: String,
    defaultProvider: ProviderEntity?,
    onNavigateToModelProviders: () -> Unit,
) {
    Column {
        SectionHeader(sectionTitle)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                ActionNavigateButton(
                    title = "模型提供方",
                    desc = "点击配置模型提供方",
                    displayMode = "",
                    onClick = onNavigateToModelProviders
                )
            }

            item {
                ActionNavigateButton(
                    title = "默认模型",
                    desc = if (defaultProvider != null) "${defaultProvider.name} / ${defaultProvider.models.firstOrNull()?.id}" else "无",
                    displayMode = "自动选择",
                    onClick = {})
            }
        }
    }
}