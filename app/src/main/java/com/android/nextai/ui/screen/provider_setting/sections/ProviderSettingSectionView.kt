package com.android.nextai.ui.screen.provider_setting.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.loading.LoadingDots
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.component.textfield.SettingPasswordField
import com.android.nextai.ui.component.textfield.SettingTextField
import com.android.nextai.viewmodel.provider.ProviderViewModel
import com.android.nextai.viewmodel.provider.entity.ProviderState

@Composable
fun ProviderSettingSectionView(
    sectionTitle: String,
    providerViewModel: ProviderViewModel,
) {

    val providerSettingState by providerViewModel.providerSettingState.collectAsState()
    val retrieveModelsState by providerViewModel.retrieveModelsState.collectAsState()
    var isApiKeyVisible by remember { mutableStateOf(false) }

    Column {

        SectionHeader(title = sectionTitle)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SettingTextField(
                    title = "名称",
                    value = providerSettingState.name,
                    placeholder = "请输入提供方名称",
                    onValueChange = {
                        providerViewModel.updateProviderSettingState(providerSettingState.copy(name = it))
                    }
                )

                SettingTextField(
                    title = "描述",
                    value = providerSettingState.desc,
                    placeholder = "添加提供方描述",
                    onValueChange = {
                        providerViewModel.updateProviderSettingState(
                            providerSettingState.copy(desc = it)
                        )
                    }
                )

                SettingTextField(
                    title = "API 地址",
                    value = providerSettingState.apiUrl,
                    placeholder = "请输入 API 地址",
                    onValueChange = {
                        providerViewModel.updateProviderSettingState(
                            providerSettingState.copy(apiUrl = it)
                        )
                    }
                )

                SettingPasswordField(
                    title = "API Key",
                    value = providerSettingState.apiKey,
                    onValueChange = {
                        providerViewModel.updateProviderSettingState(
                            providerSettingState.copy(apiKey = it)
                        )
                    },
                    passwordVisible = isApiKeyVisible,
                    onPasswordVisibleChange = { isApiKeyVisible = !isApiKeyVisible },
                    placeholder = "请输入 API Key"
                )

                RetrieveModelsButton(
                    enabled = providerSettingState.apiUrl.trim().isNotEmpty()
                            && providerSettingState.apiKey.trim().isNotEmpty(),
                    isRetrieving = retrieveModelsState is ProviderState.RetrievingModels,
                    onClick = {
                        providerViewModel.retrieveModels(
                            apiUrl = providerSettingState.apiUrl.trim(),
                            apiKey = providerSettingState.apiKey.trim(),
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun RetrieveModelsButton(
    enabled: Boolean = true,
    isRetrieving: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
            if (isRetrieving) {
                LoadingDots(dotsColor = LocalContentColor.current)
            } else {
                Text(
                    text = "获取可选模型列表",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}