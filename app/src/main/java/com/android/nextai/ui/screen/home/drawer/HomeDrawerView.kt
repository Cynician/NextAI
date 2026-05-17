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
import com.android.nextai.ui.screen.home.drawer.views.BatchActionView
import com.android.nextai.ui.screen.home.drawer.views.SessionHeaderView
import com.android.nextai.ui.screen.home.drawer.views.SessionItemView
import com.android.nextai.ui.screen.home.drawer.views.StartNewSessionView
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.entity.SessionGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerView(
    onStartNewSession: () -> Unit,
    onSessionItemClick: (Long) -> Unit,
    chatViewModel: ChatViewModel,
) {
    val isSessionLoading by chatViewModel.sessionHolder.isLoading.collectAsState()
    val groupedSessions by chatViewModel.sessionHolder.groupedSessions.collectAsState()
    val isBatchSelectMode by chatViewModel.sessionHolder.isBatchSelectMode.collectAsState()
    val batchSelectedIdSet by chatViewModel.sessionHolder.batchSelectedIdSet.collectAsState()
    val currentSessionId = chatViewModel.sessionHolder.curSessionId.collectAsState()
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    BackHandler(isBatchSelectMode) {
        chatViewModel.sessionHolder.exitBatchSelectMode()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                TopAppBar(title = { Text("NextAI", style = MaterialTheme.typography.titleLarge) })
                StartNewSessionView(onClick = { onStartNewSession() })
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
                                selectedIdSet = batchSelectedIdSet,
                                isSelectionMode = isBatchSelectMode,
                                isExpand = isExpand,
                                onToggleExpand = { expandedMap[group.name] = !isExpand },
                                onSelectGroup = { isCheck ->
                                    chatViewModel.sessionHolder.toggleGroupSelect(
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
                                    isActive = currentSessionId.value == session.id,
                                    isPinned = group.name == SessionGroup.PINNED.name,
                                    isSelectionMode = isBatchSelectMode,
                                    isSelected = batchSelectedIdSet.contains(session.id),
                                    onClick = {
                                        onSessionItemClick(session.id)
                                    },
                                    onLongClick = {
                                        chatViewModel.sessionHolder.enterBatchSelectMode(session.id)
                                    },
                                    onUnpinnedClick = {
                                        chatViewModel.unpinnedSession(session.id)
                                    }
                                )
                            }
                        }
                    }
                }
                BatchActionView(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isSelectionMode = isBatchSelectMode,
                    onDelete = { chatViewModel.batchDeleteSessions(batchSelectedIdSet.toList()) },
                    onPin = { chatViewModel.batchPinSessions(batchSelectedIdSet.toList()) },
                    onCancel = { chatViewModel.sessionHolder.exitBatchSelectMode() },
                )
            }
        }
    }
}
