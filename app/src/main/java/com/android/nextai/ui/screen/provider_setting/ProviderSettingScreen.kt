package com.android.nextai.ui.screen.provider_setting

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.component.button.ActionTextButton
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.provider_setting.sections.ProviderModelsSectionView
import com.android.nextai.ui.screen.provider_setting.sections.ProviderSettingSectionView
import com.android.nextai.viewmodel.provider.ProviderViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.android.nextai.domain.database.datastore.entity.ProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingScreen(
    onBackClick: () -> Unit,
    providerViewModel: ProviderViewModel,
) {

    val focusManager = LocalFocusManager.current

    val curProvider by providerViewModel.curProvider.collectAsState()
    val isProviderSettingChanged by providerViewModel.isProviderSettingChanged.collectAsState(false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            },
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = {
                        Text(
                            text = "模型提供方设置",
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
                    },
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Standard.screenContainerPadding)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    ActionTextButton(
                        text = "保存",
                        enabled = isProviderSettingChanged,
                        icon = null,
                        onClick = {
                            providerViewModel.saveProviderSetting()
                        }
                    )

                    ActionTextButton(
                        text = "删除",
                        enabled = curProvider != null && curProvider?.type == ProviderType.OTHER,
                        icon = null,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = {
                            curProvider?.id?.let {
                                providerViewModel.deleteProvider(it)
                                onBackClick()
                            }
                        }
                    )
                }
            },
        ) { padding ->
            /** Why use LazyColumn?
             *
             *  If the search bar is at the bottom, clicking it will pop up and be blocked by the
             *  keyboard when clicked. Using LazyColumn is convenient for implementing .imePadding().
             */
            LazyColumn (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = padding)
                    .padding(Standard.screenContainerPadding),
                verticalArrangement = Arrangement.spacedBy(Standard.screenSectionSpacing)
            ) {

                item{
                    ProviderSettingSectionView(
                        sectionTitle = "提供方设置",
                        providerViewModel = providerViewModel,
                    )
                }

                item{
                    ProviderModelsSectionView(
                        sectionTitle = "模型列表",
                        providerViewModel = providerViewModel,
                    )
                }
            }
        }
    }
}