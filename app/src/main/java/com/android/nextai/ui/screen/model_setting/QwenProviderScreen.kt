/**
 * QwenProviderScreen.kt
 */
package com.android.nextai.ui.screen.model_setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.model_setting.sections.ModelConfigSectionView
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
    onBackClick: () -> Unit,
) {

    var apiUrl by remember { mutableStateOf("https://dashscope.aliyuncs.com/compatible-mode/v1") }
    var apiToken by remember { mutableStateOf("") }
    var customModelName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var configured by remember { mutableStateOf(false) }
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
                        onClick = { }
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
                        onClick = {
                            customModelName = selectedModel
                        },
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
                        onClick = {

                        },
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
                    apiUrl = apiUrl,
                    apiToken = apiToken,
                    customModelName = customModelName,
                    configured = configured,
                    isTesting = isTesting,
                    passwordVisible = passwordVisible,
                    onApiUrlChange = { apiUrl = it },
                    onApiTokenChange = { apiToken = it },
                    onCustomModelNameChange = { customModelName = it },
                    onPasswordVisibleChange = { passwordVisible = !passwordVisible },
                    onTestClick = {
                        if (isTesting) return@ModelConfigSectionView
                        isTesting = true
                    }
                )
            }

            item { SectionHeader(title = "选择模型") }

            items(modelSeries) { item ->
                ModelSeriesItem(
                    modelSeries = item,
                    selectedModel = selectedModel,
                    onModelSelected = {
                        selectedModel = it
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    LaunchedEffect(isTesting) {
        if (isTesting) {
            delay(2500)
            configured = true
            isTesting = false
        }
    }
}

@Composable
private fun ModelSeriesItem(
    modelSeries: ModelSeries,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = modelSeries.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = modelSeries.desc,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = if (expanded) {
                        SettingsIcon.KeyboardArrowUp
                    } else {
                        SettingsIcon.KeyboardArrowDown
                    }, contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider()
                    modelSeries.models.forEach { model ->
                        ModelItem(
                            model = model,
                            isSelected = model == selectedModel,
                            onClick = onModelSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelItem(
    model: String,
    isSelected: Boolean = false,
    onClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(model)
            }
            .padding(
                horizontal = 16.dp, vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = model,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "点击选择该模型",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(16.dp)
                .border(
                    width = 1.5.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = CircleShape
                ), contentAlignment = Alignment.Center
        ) {

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            MaterialTheme.colorScheme.primary, CircleShape
                        )
                )
            }
        }
    }
}