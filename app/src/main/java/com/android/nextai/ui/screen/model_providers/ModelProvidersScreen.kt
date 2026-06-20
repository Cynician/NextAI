package com.android.nextai.ui.screen.model_providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.domain.model.provider.ProviderType
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.component.button.ActionTextButton
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.model_providers.views.ModelProviderItem
import com.android.nextai.viewmodel.provider.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelProvidersScreen(
    providerViewModel: ProviderViewModel,
    onBackClick: () -> Unit,
    onNavigateToProviderSetting: () -> Unit,
) {

    val providers by providerViewModel.providers.collectAsState(emptyList())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    Text(
                        text = "模型提供方",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    ActionButton(
                        icon = SettingsIcon.ArrowBackIosNew,
                        contentDescription = "Back",
                        onClickListener = onBackClick,
                    )
                }
            )
        },
        bottomBar = {
            BottomBar(
                onAddModelProviderClick = {
                    onNavigateToProviderSetting()
                    providerViewModel.addProviderInit()
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = providers,
                key = { it.id }
            ) { provider ->

                val icon = when(provider.type){
                    ProviderType.QWEN -> SettingsIcon.Qianwen
                    ProviderType.CLAUDE -> SettingsIcon.Claude
                    ProviderType.OPENAI -> SettingsIcon.OpenAI
                    else -> SettingsIcon.Settings
                }

                ModelProviderItem(
                    provider = provider,
                    icon = icon,
                    onClick = {
                        onNavigateToProviderSetting()
                        providerViewModel.selectCurrentProvider(provider.id)
                    }
                )
            }
        }
    }
}


@Composable
private fun BottomBar(
    onAddModelProviderClick: () -> Unit,
) {

    Surface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            ActionTextButton(
                text = "添加模型提供方",
                icon = SettingsIcon.Add,
                onClick = onAddModelProviderClick
            )
        }
    }
}
