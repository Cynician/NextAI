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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.android.nextai.ui.screen.home.body.HomeBody
import com.android.nextai.ui.screen.home.bottombar.HomeBottomBar
import com.android.nextai.ui.screen.home.drawer.HomeDrawerScreen
import com.android.nextai.ui.screen.home.topbar.HomeTopBar
import com.android.nextai.viewmodel.chat.ChatViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
) {
    // Drawer
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isSelectionMode by chatViewModel.sessionHolder.isSelectionMode.collectAsState()
    BackHandler(drawerState.isOpen && !isSelectionMode) {
        scope.launch {
            drawerState.close()
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.85f)) {
                HomeDrawerScreen(
                    onStartNewSession = {
                        chatViewModel.initSession()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onSessionSelected = {
                        if (isSelectionMode) {
                            chatViewModel.sessionHolder.toggleSelection(it)
                        } else {
                            // TODO: Load session
                            chatViewModel.sessionHolder.updateCurSessionId(it)
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
                HomeTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onTitleClick = { true },
                    onSettingsClick = { null },
                    onStoreButtonClicked = { null },
                )
            },
            bottomBar = {
                HomeBottomBar(chatViewModel = chatViewModel)
            }
        ) { paddingValues ->
            HomeBody(
                paddingValues = paddingValues,
                chatViewModel = chatViewModel
            )
        }
    }

}
