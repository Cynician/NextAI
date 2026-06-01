package com.android.nextai.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.settings.sections.ModelConfigSectionView
import com.android.nextai.viewmodel.provider.ProviderViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    providerViewModel: ProviderViewModel,
    onBackClick: () -> Unit,
    onNavigateToModelProviders: () -> Unit,
) {

    val defaultProvider by providerViewModel.defaultProvider.collectAsState(null)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    Text(
                        text = "设置",
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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(Standard.screenContainerPadding),
            verticalArrangement = Arrangement.spacedBy(Standard.screenSectionSpacing)
        ) {

            ModelConfigSectionView(
                sectionTitle = "模型配置",
                defaultProvider = defaultProvider,
                onNavigateToModelProviders = onNavigateToModelProviders,
            )
        }
    }
}