package com.android.nextai.ui.screen.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                            chatViewModel.sessionHolder.onToggleSessionSelectState(it)
                        } else {
                            scope.launch {
                                drawerState.close()
                                chatViewModel.onChangingSession(sessionId = it)
                            }
                        }
                    },
                    chatViewModel = chatViewModel,
                )
            }
        }
    ) {


        /**
         * The floating bottom bar is rendered inside the Scaffold content (not as a
         * bottomBar slot), exactly like BatchActionBar in ProviderSettingScreen.
         *  - Scaffold gets `.imePadding()` so the whole scaffold rises with the keyboard.
         *  - HomeBottomBar is aligned to BottomCenter and floats above the body.
         *  - The bar reports its real height via onHeightChanged; that value is forwarded
         *   to HomeBodyView as `bottomBarHeight` so the LazyColumn content padding can
         *   follow the bar dynamically (e.g. when it grows with multi-line input).
         */
        var bottomBarHeight by remember { mutableStateOf(96.dp) }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                HomeTopBarView(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onTitleClick = { true },
                    onSettingsClick = { onNavigateToSettings() },
                    onStoreButtonClicked = { null },
                )
            }
        ) { paddingValues ->

            Box(modifier = Modifier.fillMaxSize()) {

                HomeBodyView(
                    paddingValues = paddingValues,
                    chatViewModel = chatViewModel,
                    bottomBarHeight = bottomBarHeight,
                )

                HomeBottomBar(
                    chatViewModel = chatViewModel,
                    providerViewModel = providerViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    onHeightChanged = { height -> bottomBarHeight = height },
                )
            }
        }
    }
}
