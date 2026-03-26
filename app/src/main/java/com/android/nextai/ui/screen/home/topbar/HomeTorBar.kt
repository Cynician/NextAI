package com.android.nextai.ui.screen.home.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.icon.AppIcon
import com.android.nextai.ui.screen.home.topbar.title.HomeTorBarTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(
    onMenuClick: () -> Unit,
    onTitleClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStoreButtonClicked: () -> Unit
) {
    val context = LocalContext.current
    CenterAlignedTopAppBar(
        title = {
            HomeTorBarTitle(
                modifier = Modifier, onShowDynamicWindow = {
                    onTitleClick()
                })
        },
        navigationIcon = {
            ActionButton(
                onClickListener = onMenuClick,
                icon = AppIcon.Menu,
                modifier = Modifier.padding(start = 6.dp)
            )
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ActionButton(
                    onClickListener = onSettingsClick,
                    icon = AppIcon.Settings,
                    modifier = Modifier.padding(end = 6.dp)
                )
                ActionButton(
                    onClickListener = {
                        onStoreButtonClicked()
                    }, icon = AppIcon.Download, modifier = Modifier.padding(end = 6.dp)
                )
            }
        })
}