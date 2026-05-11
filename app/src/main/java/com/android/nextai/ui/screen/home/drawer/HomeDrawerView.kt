package com.android.nextai.ui.screen.home.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.loading.PageLoadingStateView
import com.android.nextai.ui.screen.home.drawer.views.EmptyStateView
import com.android.nextai.ui.screen.home.drawer.views.SelectionBottomBarView
import com.android.nextai.ui.screen.home.drawer.views.SessionHeaderView
import com.android.nextai.ui.screen.home.drawer.views.SessionItemView
import com.android.nextai.ui.screen.home.drawer.views.StartNewSession
import com.android.nextai.viewmodel.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerView(
    onStartNewSession: () -> Unit,
    onSessionSelected: (Long) -> Unit,
    chatViewModel: ChatViewModel,
) {
    val isSessionLoading by chatViewModel.sessionHolder.isLoading.collectAsState()
    val groupedSessions by chatViewModel.sessionHolder.groupedSessions.collectAsState()
    val isSelectionMode by chatViewModel.sessionHolder.isSelectionMode.collectAsState()
    val selectedSessionIdSet by chatViewModel.sessionHolder.selectedIdSet.collectAsState()
    val currentSessionId by chatViewModel.sessionHolder.curSessionId.collectAsState()
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    BackHandler(isSelectionMode) {
        chatViewModel.sessionHolder.exitSelectionMode()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                TopAppBar(title = { Text("NextAI", style = MaterialTheme.typography.titleLarge) })
                StartNewSession({
                    onStartNewSession()
                })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            if (isSessionLoading) {
                PageLoadingStateView()
            } else if (groupedSessions.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn {
                    groupedSessions.forEach { (group, sessionList) ->
                        val isExpand = expandedMap[group.name] ?: true
                        stickyHeader {
                            SessionHeaderView(
                                title = group.title,
                                sessions = sessionList,
                                selectedIdSet = selectedSessionIdSet,
                                isSelectionMode = isSelectionMode,
                                isExpand = isExpand,
                                onToggleExpand = { expandedMap[group.name] = !isExpand },
                                onSelectGroup = { isCheck ->
                                    chatViewModel.sessionHolder.toggleGroupSelection(
                                        group = group,
                                        sessions = sessionList,
                                        isCheck = isCheck
                                    )
                                },
                            )
                        }

                        if (isExpand) {
                            items(sessionList, key = { it.id }) { session ->
                                SessionItemView(
                                    session = session,
                                    isActive = currentSessionId == session.id,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedSessionIdSet.contains(session.id),
                                    onClick = {
                                        onSessionSelected(session.id)
                                    },
                                    onLongClick = {
                                        chatViewModel.sessionHolder.enterSelectionMode(session.id)
                                    }
                                )
                            }
                        }
                    }
                }
                SelectionBottomBarView(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isSelectionMode = isSelectionMode,
                    onDelete = { chatViewModel.batchDeleteSessions(selectedSessionIdSet.toList()) },
                    onPin = { chatViewModel.batchPinSessions(selectedSessionIdSet.toList()) },
                    onCancel = { chatViewModel.sessionHolder.exitSelectionMode() },
                )
            }
        }
    }
}
