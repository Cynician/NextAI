package com.android.nextai.ui.screen.provider_setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.domain.database.datastore.entity.ProviderType
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.ui.screen.provider_setting.sections.ProviderModelsSectionView
import com.android.nextai.ui.screen.provider_setting.sections.ProviderSettingSectionView
import com.android.nextai.viewmodel.provider.ProviderViewModel

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
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.changedToDown() }) {
                            focusManager.clearFocus()
                        }
                    }
                }
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
            }
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
                contentPadding = PaddingValues(
                    bottom = 80.dp
                ),
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

            val canDelete = curProvider?.type == ProviderType.OTHER

            BatchActionBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding(),
                visible = (canDelete || isProviderSettingChanged),
                enableDelete = canDelete,
                enableReset = isProviderSettingChanged,
                enableSave = isProviderSettingChanged,
                onDelete = {
                    curProvider?.id?.let {
                        providerViewModel.deleteProvider(it)
                        onBackClick()
                    }
                },
                onReset = {
                    providerViewModel.resetProviderSetting()
                },
                onSave = {
                    providerViewModel.saveProviderSetting()
                }
            )
        }
    }
}


@Composable
fun BatchActionBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    enableDelete: Boolean,
    enableReset: Boolean,
    enableSave: Boolean,
    onDelete: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ActionItem(
                    enabled = enableDelete,
                    icon = SettingsIcon.Delete,
                    text = "删除",
                    tint =  MaterialTheme.colorScheme.error.copy(0.7f),
                    onClick = onDelete
                )
                ActionItem(
                    enabled = enableReset,
                    icon = SettingsIcon.Refresh,
                    text = "重置",
                    onClick = onReset,
                )
                ActionItem(
                    enabled = enableSave,
                    icon = SettingsIcon.Save,
                    text = "保存",
                    onClick = onSave
                )
            }
        }
    }


}

@Composable
fun ActionItem(
    enabled: Boolean = true,
    icon: ImageVector?,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if(enabled) tint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            color = if(enabled) tint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            style = MaterialTheme.typography.labelLarge
        )
    }
}