package com.android.nextai.ui.screen.home.body

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.icon.AppIcon
import com.android.nextai.ui.screen.home.body.bubble.AssistantMessageBubbleList
import com.android.nextai.ui.screen.home.body.bubble.UserMessageBubble
import com.android.nextai.ui.theme.Animation
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.entity.Role
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

private const val TAG = "HomeBody"

@OptIn(ExperimentalSharedTransitionApi::class, FlowPreview::class)
@Composable
fun HomeBody(
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
) {
    val isGenerating by chatViewModel.messageHolder.isGenerating.collectAsState()
    val messageList = chatViewModel.messageHolder.messageList
    val isTextStreaming by chatViewModel.messageHolder.isTextStreaming.collectAsState()
    val streamingTick = chatViewModel.uiStateHolder.streamingTick.collectAsState()
    val listState = rememberLazyListState()

    /**
     * scroll logic
     */
    var shouldAutoScroll by remember { mutableStateOf(true) }
    var isUserScrolling by remember { mutableStateOf(false) }
    val scrollToHeadAssistMessage by chatViewModel.uiStateHolder.scrollToHeadAssistMessage.collectAsState()
    // record whether the user actively triggers scrolling
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    isUserScrolling = true
                    shouldAutoScroll = false
                }
                return Offset.Zero
            }
        }
    }
    // scroll to the latest assistant message
    LaunchedEffect(scrollToHeadAssistMessage) {
        Log.d(TAG, "updateScrollToAssistMessage : $scrollToHeadAssistMessage")
        val itemCount = listState.layoutInfo.totalItemsCount
        if (itemCount > 0 && scrollToHeadAssistMessage) {
            listState.scrollToItem(itemCount - 1)
        }
        isUserScrolling = false
        shouldAutoScroll = true
    }
    // auto scroll
    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                shouldAutoScroll,
                isUserScrolling,
                streamingTick.value
            )
        }.debounce(20)
            .collect {
                if (
                    shouldAutoScroll &&
                    isTextStreaming &&
                    !isUserScrolling
                ) {
                    listState.scrollBy(1000f)
                }
            }
    }
    /**
     * layout
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        if (isGenerating) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(vertical = Standard.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(Standard.SpacingXs)

            ) {
                if (isTextStreaming) {
                    messageList.forEachIndexed { index, message ->
                        when (message.role) {
                            Role.User -> {
                                item(key = "${message.msgId}-user") {
                                    UserMessageBubble(message.blocks[0])
                                }
                            }

                            Role.Assistant -> {
                                item(key = "${message.msgId}-assistant") {
                                    AssistantMessageBubbleList(message.blocks)
                                }
                            }

                            Role.None -> {}
                        }
                    }
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
                imageVector = AppIcon.User,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(0.4f)
            )
            Spacer(Modifier.height(Standard.SpacingLg))
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