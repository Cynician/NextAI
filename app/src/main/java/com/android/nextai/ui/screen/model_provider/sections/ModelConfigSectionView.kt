package com.android.nextai.ui.screen.model_provider.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.loading.LoadingDots
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.component.other.SuccessTipLabel
import com.android.nextai.ui.component.textfield.SettingPasswordField
import com.android.nextai.ui.component.textfield.SettingTextField

@Composable
fun ModelConfigSectionView(
    title: String = "",
    apiUrl: String,
    apiToken: String,
    customModelName: String,
    isValid: Boolean,
    isValidating: Boolean,
    passwordVisible: Boolean,
    onApiUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onCustomModelNameChange: (String) -> Unit,
    onPasswordVisibleChange: () -> Unit,
    onValidateClick: () -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SectionHeader(title = title)
        SuccessTipLabel(
            visible = isValid,
            text = "有效"
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

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
                title = "API 地址",
                value = apiUrl,
                placeholder = "请输入 API 地址",
                onValueChange = onApiUrlChange
            )

            SettingPasswordField(
                title = "API Key",
                value = apiToken,
                onValueChange = onApiKeyChange,
                passwordVisible = passwordVisible,
                onPasswordVisibleChange = onPasswordVisibleChange,
                placeholder = "请输入 API Key"
            )

            SettingTextField(
                title = "模型名称",
                value = customModelName,
                placeholder = "请输入模型名称",
                onValueChange = onCustomModelNameChange
            )
            CompositionLocalProvider(
                LocalRippleConfiguration provides null
            ) {
                ProviderValidateButton(
                    isValidating = isValidating,
                    onClick = {
                        onValidateClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun ProviderValidateButton(
    isValidating: Boolean = false,
    onClick: ()->Unit,
){
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer) {
            if (isValidating) {
                LoadingDots(dotsColor = LocalContentColor.current)
            } else {
                Text(
                    text = "验       证",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
