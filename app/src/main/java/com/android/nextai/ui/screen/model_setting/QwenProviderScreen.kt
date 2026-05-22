/**
 * QwenProviderScreen.kt
 */
package com.android.nextai.ui.screen.model_setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.model_setting.sections.ModelConfigSectionView
import com.android.nextai.ui.screen.model_setting.sections.ModelSelectSectionView
import com.android.nextai.viewmodel.provider.ProviderViewModel
import kotlinx.coroutines.delay

data class ModelSeries(
    val title: String,
    val desc: String,
    val models: List<String>,
    val recommend: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QwenProviderScreen(
    providerViewModel: ProviderViewModel,
    onBackClick: () -> Unit,
) {
    val provider by providerViewModel.curProvider.collectAsState()

    var apiUrl by remember {
        mutableStateOf(
            provider?.apiUrl?: "https://dashscope.aliyuncs.com/compatible-mode/v1"
        )
    }
    var apiKey by remember { mutableStateOf(provider?.apiKey?:"") }
    var model by remember { mutableStateOf(provider?.model?:"") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var isOK by remember { mutableStateOf(provider?.isOK?:false) }
    var selectedModel by remember { mutableStateOf("Qwen3-30B-A3B") }

    val modelSeries = remember {
        listOf(
            ModelSeries(
                title = "Qwen3", desc = "千问3系列", models = listOf(
                    "Qwen3-30B-A3B", "Qwen3-14B", "Qwen3-7B", "Qwen3-1.7B"
                )
            ), ModelSeries(
                title = "Qwen2.5", desc = "千问2.5系列", models = listOf(
                    "Qwen2.5-72B-Instruct", "Qwen2.5-32B-Instruct"
                ), recommend = true
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(horizontal = 12.dp),
                title = {
                    Text(
                        text = "通义千问",
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
                actions = {
                    TextButton(
                        onClick = {
                            providerViewModel.updateProvider(
                                apiUrl = apiUrl.trim(),
                                apiKey = apiKey.trim(),
                                model = model.trim(),
                                isOK = isOK
                            )
                        }
                    ) {
                        Text(
                            text = "保存",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                onSetModelClick = {model = selectedModel },
                onModelDetailsClick = {}
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            item {
                ModelConfigSectionView(
                    title = "配置模型",
                    apiUrl = apiUrl,
                    apiToken = apiKey,
                    customModelName = model,
                    configured = isOK,
                    isTesting = isTesting,
                    passwordVisible = passwordVisible,
                    onApiUrlChange = { apiUrl = it },
                    onApiTokenChange = { apiKey = it },
                    onCustomModelNameChange = { model = it },
                    onPasswordVisibleChange = { passwordVisible = !passwordVisible },
                    onTestClick = {
                        if (isTesting) return@ModelConfigSectionView
                        isTesting = true
                    }
                )
            }

            ModelSelectSectionView(
                title = "选择模型",
                modelSeries = modelSeries,
                selectedModel = selectedModel,
                onModelSelected = {selectedModel = it}
            )

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    LaunchedEffect(isTesting) {
        if (isTesting) {
            delay(2500)
            isOK = true
            isTesting = false
        }
    }
}

@Composable
private fun BottomBar(
    onSetModelClick: () -> Unit,
    onModelDetailsClick: () -> Unit,
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

            Button(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
                onClick = onSetModelClick,
            ) {
                Text(
                    text = "设为默认模型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = onModelDetailsClick,
            ) {
                Text(
                    text = "查看模型详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}