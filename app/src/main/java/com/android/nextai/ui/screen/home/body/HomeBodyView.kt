package com.android.nextai.ui.screen.home.body

import com.android.nextai.ui.component.loading.SequentialJumpingDots
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.icon.HomeIcon
import com.android.nextai.ui.screen.home.body.views.AssistantBubbleView
import com.android.nextai.ui.component.loading.LoadingOverlayView
import com.android.nextai.ui.screen.home.body.views.UserBubbleView
import com.android.nextai.ui.theme.Animation
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.entity.Role
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalSharedTransitionApi::class, FlowPreview::class)
@Composable
fun HomeBodyView(
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
) {
    val isGenerating by chatViewModel.messageHolder.isGenerating.collectAsState(initial = false)
    val messageList by chatViewModel.messageHolder.messageList.collectAsState()
    val listState = rememberLazyListState()

    /**
     * Load messages
     */
    val isChangingSession by chatViewModel.messageHolder.isLoadingFirst.collectAsState()
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            firstVisible
        }
            .distinctUntilChanged()
            .collect { index ->
                if (index <= 2) {
                    chatViewModel.loadNextPageMessages()
                }
            }
    }

    /**
     * Scroll logic
     */
    var shouldAutoScroll by rememberSaveable { mutableStateOf(true) }
    var isUserScrolling by remember { mutableStateOf(false) }
    // Scroll to the latest user message
    LaunchedEffect(Unit) {
        chatViewModel
            .messageHolder
            .scrollToLatestMessageEvent
            .collect {
                if (messageList.isEmpty()) {
                    return@collect
                }
                listState.scrollToItem(messageList.lastIndex, Int.MAX_VALUE)
                isUserScrolling = false
                shouldAutoScroll = true
            }
    }
    // User actively triggers scrolling
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    isUserScrolling = true
                    shouldAutoScroll = false
                }
                return Offset.Zero
            }
        }
    }
    // Auto scroll logic
    val density = LocalDensity.current
    val bottomPaddingPx = remember(paddingValues) {
        with(density) {
            paddingValues.calculateBottomPadding().toPx()
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow {
            messageList.lastOrNull()?.content
        }.collect {
            while (shouldAutoScroll && !isUserScrolling) {
                val layoutInfo = listState.layoutInfo
                val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
                if (lastItem == null) break
                val visibleBottom = layoutInfo.viewportEndOffset - bottomPaddingPx
                val isBottomReached =
                    lastItem.index == messageList.lastIndex &&
                            lastItem.offset + lastItem.size <= visibleBottom
                if (isBottomReached) { break }
                listState.scrollBy(12f)
                awaitFrame()
            }
        }
    }

    /**
     * Layout
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        ) {

        LoadingOverlayView(isChangingSession)

        if (messageList.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .padding(12.dp),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(
                    items = messageList,
                    key = { _, item -> item.id }
                ) { index, message ->
                    when (message.role) {
                        Role.User.name -> {
                            UserBubbleView(message.content)
                        }
                        Role.Assistant.name -> {
                            if(message.content.isEmpty()) return@itemsIndexed
                            AssistantBubbleView(
                                parser = chatViewModel.markdownCacheHolder.getOrCreate(message.id)
                            )
                        }
                        Role.None.name -> {}
                    }
                }
                if(isGenerating && messageList.last().content.isEmpty()){
                    item{ SequentialJumpingDots() }
                }
            }
        } else {
            EmptyMessageView()
        }

        MaskView()
    }

}

@Composable
internal fun EmptyMessageView() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = HomeIcon.User,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "有什么可以帮忙的？",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}

@Composable
internal fun MaskView() {
    AnimatedVisibility(
        visible = false,
        enter = fadeIn(Animation.entrance()),
        exit = fadeOut(Animation.exit())
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {}
            )
        }
    }
}