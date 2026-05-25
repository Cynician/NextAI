/**
 * QwenProviderScreen.kt
 */
package com.android.nextai.ui.screen.model_setting

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.domain.database.data.QwenModels
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.component.other.NoticeBubble
import com.android.nextai.ui.component.other.NoticeBubbleData
import com.android.nextai.ui.component.other.NoticeType
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.model_setting.sections.ModelConfigSectionView
import com.android.nextai.ui.screen.model_setting.sections.ModelSelectSectionView
import com.android.nextai.viewmodel.provider.ProviderViewModel
import com.android.nextai.viewmodel.provider.entity.ProviderValidateEvent
import com.android.nextai.viewmodel.provider.entity.ProviderValidateState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QwenProviderScreen(
    providerViewModel: ProviderViewModel,
    onBackClick: () -> Unit,
) {
    val provider by providerViewModel.curProvider.collectAsState()

    var initialApiUrl = provider?.apiUrl?.takeIf { it.isNotBlank() }
        ?: "https://dashscope.aliyuncs.com/compatible-mode/v1"
    var initialApiKey = provider?.apiKey ?: ""
    var initialModel = provider?.model ?: ""
    var apiUrl by remember(provider) { mutableStateOf(initialApiUrl) }
    var apiKey by remember(provider) { mutableStateOf(initialApiKey) }
    var model by remember(provider) { mutableStateOf(initialModel) }
    var isOK by remember { mutableStateOf(false) }
    val hasChanges by remember(
        apiUrl, apiKey, model, initialApiUrl, initialApiKey, initialModel
    ) {
        derivedStateOf {
            apiUrl.trim() != initialApiUrl || apiKey.trim() != initialApiKey || model.trim() != initialModel
        }
    }

    val modelSeries = remember { QwenModels.allSeries }
    var selectedModel by remember { mutableStateOf(initialModel) }

    var passwordVisible by remember { mutableStateOf(false) }

    /**
     * Save button & notice bubble
     */
    val scope = rememberCoroutineScope()
    var isNoticeBubbleVisible by remember { mutableStateOf(false) }
    var noticeData by remember { mutableStateOf<NoticeBubbleData?>(null) }
    fun showNoticeBubble(message: String, type: NoticeType) {
        if (noticeData != null) return
        scope.launch {
            noticeData = NoticeBubbleData(message = message, type = type)
            isNoticeBubbleVisible = true
            delay(1200)
            isNoticeBubbleVisible = false
            delay(300)
            noticeData = null
        }
    }
    LaunchedEffect(Unit) {
        providerViewModel.saveProviderState.collect { result ->
            result.onSuccess {
                showNoticeBubble(message = "保存成功", type = NoticeType.SUCCESS)
            }.onFailure {
                showNoticeBubble(message = "保存失败", type = NoticeType.ERROR)
            }
        }
    }

    val focusManager = LocalFocusManager.current

    /**
     * Check validate state of provider
     */
    val providerValidateState by providerViewModel.providerValidateState.collectAsState()
    LaunchedEffect(providerValidateState) {
        providerViewModel.providerValidateEvent.collect { event ->
            when (event) {
                ProviderValidateEvent.Success -> {
                    showNoticeBubble(
                        message = "验证成功", type = NoticeType.SUCCESS
                    )
                }

                is ProviderValidateEvent.Error -> {
                    showNoticeBubble(
                        message = event.message, type = NoticeType.ERROR
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    focusManager.clearFocus()
                }
            )
        },
    ) {
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
                        SaveButton(
                            visible = hasChanges,
                            onClick = {
                                initialApiUrl = apiUrl.trim()
                                initialApiKey = apiKey.trim()
                                initialModel = model.trim()
                                providerViewModel.updateProvider(
                                    apiUrl = initialApiUrl,
                                    apiKey = initialApiKey,
                                    model = initialModel,
                                    isOK = isOK
                                )
                                focusManager.clearFocus()
                            }
                        )
                    }
                )
            },
            bottomBar = {
                BottomBar(
                    onSetModelClick = { model = selectedModel },
                    onModelDetailsClick = {}
                )
            },
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
                        isValidating = providerValidateState is ProviderValidateState.Validating,
                        passwordVisible = passwordVisible,
                        onApiUrlChange = { apiUrl = it },
                        onApiKeyChange = { apiKey = it },
                        onCustomModelNameChange = { model = it },
                        onPasswordVisibleChange = { passwordVisible = !passwordVisible },
                        onValidateClick = {
                            if (providerValidateState is ProviderValidateState.Validating) {
                                return@ModelConfigSectionView
                            }
                            providerViewModel.checkModelValidity(
                                apiUrl = apiUrl, apiKey = apiKey, model = model
                            )
                            focusManager.clearFocus()
                        }
                    )
                }

                ModelSelectSectionView(
                    title = "选择模型",
                    modelSeries = modelSeries,
                    selectedModel = selectedModel,
                    onModelSelected = { selectedModel = it.modelName })

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        NoticeBubble(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = isNoticeBubbleVisible,
            data = noticeData,
        )
    }
}

@Composable
private fun SaveButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    if (!visible) return

    TextButton(
        onClick = onClick
    ) {
        Text(
            text = "保存",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
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