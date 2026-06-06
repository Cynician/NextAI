package com.android.nextai.ui.screen.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.android.nextai.ui.screen.home.body.HomeBodyView
import com.android.nextai.ui.screen.home.bottombar.HomeBottomBar
import com.android.nextai.ui.screen.home.drawer.HomeDrawerView
import com.android.nextai.ui.screen.home.topbar.HomeTopBarView
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.provider.ProviderViewModel
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    chatViewModel: ChatViewModel,
    providerViewModel: ProviderViewModel,
    onNavigateToSettings: () -> Unit,
) {
    /** Drawer's coroutines cope. **/
    val scope = rememberCoroutineScope()

    /** The drawer status is initially closed. **/
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    /** Check whether the current mode is batch selection. **/
    val isBatchSelectMode by chatViewModel.sessionHolder.isBatchSelectMode.collectAsState()

    // In batch selection mode, press the back key to exit.
    BackHandler(drawerState.isOpen && !isBatchSelectMode) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                HomeDrawerView(
                    onStartNewSession = {
                        scope.launch {
                            drawerState.close()
                            chatViewModel.initSession()
                        }
                    },
                    onSessionItemClick = {
                        if (isBatchSelectMode) {
                            chatViewModel.sessionHolder.toggleItemSelect(it)
                        } else {
                            scope.launch {
                                drawerState.close()
                                chatViewModel.loadFirstPageMessages(sessionId = it)
                            }
                        }
                    },
                    chatViewModel = chatViewModel,
                )
            }
        }
    ) {

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HomeTopBarView(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onTitleClick = { true },
                    onSettingsClick = { onNavigateToSettings() },
                    onStoreButtonClicked = { null },
                )
            },
            bottomBar = {
                HomeBottomBar(
                    chatViewModel = chatViewModel,
                    providerViewModel = providerViewModel,
                )
            }
        ) { paddingValues ->

            HomeBodyView(
                paddingValues = paddingValues,
                chatViewModel = chatViewModel,
            )
        }
    }
}
