package com.android.nextai.ui.screen.home.body

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.nextai.R
import com.android.nextai.domain.model.Role
import com.android.nextai.ui.component.loading.LoadingOverlayView
import com.android.nextai.ui.component.loading.SequentialJumpingDots
import com.android.nextai.ui.screen.home.body.views.AssistantBubbleView
import com.android.nextai.ui.screen.home.body.views.UserBubbleView
import com.android.nextai.ui.theme.Animation
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.state.ChatSessionState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


@OptIn(ExperimentalSharedTransitionApi::class, FlowPreview::class)
@Composable
fun HomeBodyView(
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
) {
    val focusManager = LocalFocusManager.current

    /** Get the current active Session ID **/
    val currentSessionId by chatViewModel.sessionHolder.curSessionId.collectAsState()

    val currentSessionState by chatViewModel.currentSessionState.collectAsState()

    val state = currentSessionState ?: ChatSessionState(sessionId = currentSessionId)
    val messageList = state.messageList
    val isGenerating = state.isGenerating
    val isChangingSession by chatViewModel.sessionHolder.isLoadingFirst.collectAsState()

    /** The core message list **/
    val listState = remember(currentSessionId) {
        val initialIndex = if (messageList.isNotEmpty()) messageList.lastIndex else 0
        LazyListState(
            firstVisibleItemIndex = initialIndex,
            firstVisibleItemScrollOffset = Int.MAX_VALUE
        )
    }


    /** Get the current interface's "screen density" and "font scaling ratio." **/
    val density = LocalDensity.current

    /**
     * This variable is the core of the list scrolling logic, used to record whether the variable
     * should automatically follow the bottom scroll.
     */
    var followBottom by remember(currentSessionId) { mutableStateOf(isGenerating) }

    /** Used to record whether the user is actively dragging the list of variables **/
    var isUserDragging by remember(currentSessionId) { mutableStateOf(false) }

    /** Triggered when the user's physical finger is just about to swipe across the screen,
     * but the list has not yet experienced any actual scrolling offset (Delta). **/
    val nestedScrollConnection = remember(currentSessionId) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {

                if (source == NestedScrollSource.UserInput) {
                    // As long as the user's finger is swiping, first mark it as dragging
                    isUserDragging = true
                    // If the user is "swiping up" (available.y < 0), automatic tracking is
                    // unconditionally cut off
                    if (available.y < 0f) {
                        followBottom = false
                    }
                }
                return Offset.Zero
            }
        }
    }

    /** Get the real-time height of the current keyboard。 **/
    val imeInsets = WindowInsets.ime

    /**
     * Cache the state of the keyboard before it pops up. If currently at the bottom of the list,
     * when the keyboard is raised, the message at the bottom should be dragged to a visible area.
     * but while keyboard raising, the bottom content will be covered, so bottom visibility will
     * always be "false". Therefore, need to cache whether the keyboard is at the bottom of the
     * list before the keyboard is raised.
     * */
    var wasBottomVisible by remember(currentSessionId) {
        mutableStateOf(
            isLastItemBottomVisible(
                listState = listState,
                density = density,
            )
        )
    }

    // Paginated loading: During upward scrolling, if conditions are met, the next page's message
    // is loaded.
    LaunchedEffect(listState, messageList, currentSessionId) {
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

    // Used to position the latest message at the end of the list after switching session.
    LaunchedEffect(listState,currentSessionId, followBottom, isGenerating) {
        chatViewModel
            .sessionHolder
            .scrollToLatestMessageEvent
            .collectLatest {targetSessionId ->
                // Prevents cross-session pollution.
                if (targetSessionId != currentSessionId || messageList.isEmpty()) {
                    return@collectLatest
                }
                if (followBottom && isGenerating) {
                    Log.d("AUTO_SCROLL", "tracking scrolling is ready, skip scrolling to the latest msg")
                    return@collectLatest
                }
                try {
                    Log.d("AUTO_SCROLL", "scrolling to the latest msg, isGenerating = $isGenerating")
                    listState.requestScrollToItem(messageList.lastIndex, Int.MAX_VALUE)

                    // Activate tracking scrolling status.
                    if (isGenerating) {
                        followBottom = true
                        isUserDragging = false
                    }
                } catch (e: Exception) {
                    Log.d("AUTO_SCROLL", "scrolling to bottom failed: ${e.message}")
                }

            }
    }

    // Monitor two metrics: whether it is currently being generated, and whether the last one
    // is visible. If it is generating and the user swipes down to make the bottom visible again,
    // the tracking status will automatically reset.
    LaunchedEffect(listState, isGenerating, currentSessionId) {
        snapshotFlow {
            isGenerating to isLastItemBottomVisible(listState = listState, density = density, tolerance = 48.dp)
        }
            .distinctUntilChanged()
            .collect { (generating, lastVisible) ->
                if (generating && lastVisible) {
                    followBottom = true
                    isUserDragging = false
                }
            }
    }

    /**
     * This allows the running while loop to "real-time" see the latest values from external `followBottom`
     * and `isUserDragging` without restarting the entire coroutine.
     */
    val currentFollowBottomState = rememberUpdatedState(followBottom)
    val currentUserDraggingState = rememberUpdatedState(isUserDragging)

    // When the condition is met, keep following the bottom of the list.
    LaunchedEffect(currentSessionId, listState, followBottom, isUserDragging) {
        if (isChangingSession || messageList.isEmpty()) return@LaunchedEffect

        Log.d("AUTO_SCROLL", "rolling engine inspection -> followBottom = $followBottom, isUserDragging = $isUserDragging")
        if (!followBottom || isUserDragging) return@LaunchedEffect

        val scrollingSpeed = 20f
        while (currentFollowBottomState.value && !currentUserDraggingState.value) {
            try {
                listState.scroll(scrollPriority = MutatePriority.Default) {
                    while (currentFollowBottomState.value && !currentUserDraggingState.value) {
                        awaitFrame()
                        scrollBy(scrollingSpeed)
                    }
                }
            } catch (e: Exception) {
                awaitFrame()
                Log.d("AUTO_SCROLL", "tracking scroll was interrupted: ${e.message}")
            }
        }

    }

    // When the last message is visible, the keyboard rises, the bottom of the list paddings
    // increases, and need to drag the bottom content to the visible area.
    var lastObservedKeyboardHeight by remember(currentSessionId) { mutableIntStateOf(0) }
    LaunchedEffect(currentSessionId, listState, isGenerating) {
        snapshotFlow {
            val currentBottomVisible = isLastItemBottomVisible(listState=listState, density=density)
            Triple(
                imeInsets.getBottom(density),
                messageList.size,
                currentBottomVisible
            )
        }   .distinctUntilChanged()
            .collect { (latestHeight, listSize, currentBottomVisible) ->
                if (latestHeight == 0) {
                    wasBottomVisible = currentBottomVisible
                }
                if(isChangingSession || currentSessionId!= currentSessionState?.sessionId || listSize == 0) return@collect

                if (latestHeight > lastObservedKeyboardHeight && followBottom) {
                    //Keyboard event detected, bottom followers are prohibited
                    followBottom = false
                }else if (latestHeight < lastObservedKeyboardHeight && latestHeight == 0 && !isUserDragging) {
                    followBottom = true
                }

                if (listSize > 0 && wasBottomVisible && latestHeight > 0) {
                    try {
                        listState.requestScrollToItem(messageList.lastIndex, Int.MAX_VALUE)
                    } catch (e: Exception) {
                        Log.d("AUTO_SCROLL", "fail to drag the content up, e: ${e.message}")
                    }
                }
                lastObservedKeyboardHeight = latestHeight
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
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

        LoadingOverlayView(visible = isChangingSession)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection)
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .visible(messageList.isNotEmpty()),
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
                        if (message.content.isEmpty()) return@itemsIndexed
                        AssistantBubbleView(
                            parser = chatViewModel.markdownCacheHolder.getOrCreate(message.id)
                        )
                    }

                    Role.None.name -> {}
                }
            }
            if (isGenerating && messageList.last().content.isEmpty()) {
                item { SequentialJumpingDots() }
            }
        }

        EmptyMessageView(
            visible = messageList.isEmpty()
        )
    }

    MaskView()
}

@Composable
internal fun EmptyMessageView(
    visible: Boolean
) {
    Box(
        modifier = Modifier
            .visible(visible)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_nextai_logo_foreground_original),
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

/**
 * Get visibility info of the last message
 *
 * tolerance: Tolerance value. Bottom conditions can sometimes be too harsh to trigger, so a
 * tolerable pixel range can be granted.
 */
fun isLastItemBottomVisible(listState: LazyListState, density: Density, tolerance: Dp = 96.dp): Boolean {
    val layoutInfo = listState.layoutInfo
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    val tolerance = with(density) { tolerance.toPx() }.toInt()
    return (lastVisibleItem.offset + lastVisibleItem.size - tolerance) <= layoutInfo.viewportEndOffset
}