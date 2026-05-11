package com.android.nextai.ui.screen.home.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.domain.database.db.entity.SessionEntity
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.checkbox.CircleCheckbox
import com.android.nextai.ui.component.loading.LoadingState
import com.android.nextai.ui.icon.AppIcon
import com.android.nextai.viewmodel.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerScreen(
    onStartNewSession: () -> Unit,
    onSessionSelected: (Long) ->Unit,
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
            ){
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
                LoadingState()
            } else if(groupedSessions.isEmpty()){
                EmptyState()
            } else {
                LazyColumn {
                    groupedSessions.forEach { (group, sessionList) ->
                        val isExpand = expandedMap[group.name] ?: true
                        stickyHeader {
                            SessionHeader(
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
                                SessionItem(
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
                SelectionBottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isSelectionMode = isSelectionMode,
                    onDelete = {chatViewModel.batchDeleteSessions(selectedSessionIdSet.toList())},
                    onPin = {chatViewModel.batchPinSessions(selectedSessionIdSet.toList())},
                    onCancel = {chatViewModel.sessionHolder.exitSelectionMode()},
                )
            }
        }
    }
}

@Composable
fun StartNewSession(
    onClick:()->Unit
){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        shape = RoundedCornerShape(Standard.RadiusMd),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth()
                .clickable { onClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ){
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = AppIcon.StartSession,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "新建对话",
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

}

@Composable
fun SessionHeader(
    title: String,
    sessions: List<SessionEntity>,
    selectedIdSet: Set<Long>,
    isSelectionMode: Boolean,
    isExpand: Boolean,
    onToggleExpand: () -> Unit,
    onSelectGroup: (Boolean) -> Unit,
) {
    val allSelected = sessions.isNotEmpty() && sessions.all { selectedIdSet.contains(it.id) }
    val rotation by animateFloatAsState(targetValue = if (isExpand) 90f else 0f, label = "arrow_rotation")

    Row(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onToggleExpand()
            }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = isSelectionMode) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides Dp.Unspecified
            ) {
                CircleCheckbox(
                    modifier = Modifier.size(18.dp),
                    checked = allSelected,
                    onCheckedChange = onSelectGroup
                )
            }
        }
        Text(
            text = title,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = AppIcon.ArrowRight,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun SessionItem(
    session: SessionEntity,
    isActive:Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val sessionItemColor by animateColorAsState(
        targetValue =
            if (isActive) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        label = ""
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        AnimatedVisibility(visible = isSelectionMode) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides Dp.Unspecified
            ) {
                CircleCheckbox(
                    modifier = Modifier.size(18.dp),
                    checked = isSelected,
                    onCheckedChange = { _ -> onClick() }
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(Standard.RadiusMd),
            color = sessionItemColor
        ) {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title ?: "Untitled",
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Standard.SpacingMd)
        ) {
            Icon(
                imageVector = AppIcon.Messages,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(0.4f)
            )
            Text(
                "暂无内容",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "快去开启对话把~",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SelectionBottomBar(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onCancel: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isSelectionMode,
        enter = slideInVertically(
            initialOffsetY = { it / 2 }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 }
        ) + fadeOut()
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
                BottomActionItem(
                    icon = AppIcon.Delete,
                    text = "删除",
                    tint = Color(0xFFCC4B4B),
                    onClick = onDelete
                )
                BottomActionItem(
                    icon = AppIcon.PushPin,
                    text = "置顶",
                    onClick = onPin
                )
                BottomActionItem(
                    icon = AppIcon.Cancel,
                    text = "取消",
                    onClick = onCancel
                )
            }
        }
    }
}

@Composable
private fun BottomActionItem(
    icon: ImageVector?,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.labelLarge
        )
    }
}