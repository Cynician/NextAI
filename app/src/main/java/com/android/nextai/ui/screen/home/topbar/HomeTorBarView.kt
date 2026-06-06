package com.android.nextai.ui.screen.home.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.icon.HomeIcon
import com.android.nextai.ui.screen.home.topbar.views.TitleView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBarView(
    onMenuClick: () -> Unit,
    onTitleClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStoreButtonClicked: () -> Unit,
) {

    val focusManager = LocalFocusManager.current

    CenterAlignedTopAppBar(
        modifier = Modifier
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
        title = {
            TitleView(
                modifier = Modifier,
                onShowDynamicWindow = {
                    onTitleClick()
                }
            )
        },
        navigationIcon = {
            ActionButton(
                modifier = Modifier.padding(start = 6.dp),
                icon = HomeIcon.Menu,
                onClickListener = onMenuClick,
            )
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ActionButton(
                    modifier = Modifier.padding(end = 6.dp),
                    icon = HomeIcon.Settings,
                    onClickListener = onSettingsClick,
                )
                ActionButton(
                    modifier = Modifier.padding(end = 6.dp),
                    icon = HomeIcon.Download,
                    onClickListener = {
                        onStoreButtonClicked()
                    },
                )
            }
        }
    )
}