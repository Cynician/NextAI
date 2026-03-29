package com.android.nextai.ui.screen.home.body

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.nextai.ui.Standard
import com.android.nextai.ui.icon.AppIcon
import com.android.nextai.ui.screen.home.utils.UiUtils.scrollToTargetBubble
import com.android.nextai.ui.theme.Animation
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.entity.Role

private const val TAG = "HomeBody"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeBody(
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
) {
    val isGenerating by chatViewModel.messageHolder.isGenerating.collectAsStateWithLifecycle()
    val messageList = chatViewModel.messageHolder.messageList
    val listState = rememberLazyListState()

    val isTextStreaming by chatViewModel.isTextStreaming.collectAsState()

    // body roll logic
    val lastUserMarkdownElementCnt by chatViewModel.messageHolder.lastUserMarkdownElementCnt.collectAsState()
    val curPrompt by chatViewModel.messageHolder.curPrompt.collectAsStateWithLifecycle()
    LaunchedEffect(messageList.size) {
        if (messageList.isEmpty() || isGenerating) return@LaunchedEffect
        val elementIndex = lastUserMarkdownElementCnt - 1
        if (elementIndex < 0) return@LaunchedEffect
        Log.d(TAG, "elementIndex: $elementIndex")
        listState.scrollToTargetBubble(bubbleIndex = elementIndex, promptLength = curPrompt.length)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        if (!isGenerating && messageList.isEmpty()) {
            EmptyMessageView()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = Standard.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(Standard.SpacingXs)
            ) {
                if(isTextStreaming){
                    messageList.forEachIndexed { index, message ->
                        when (message.role) {
                            Role.User -> {

                                item(key = "${message.msgId}-user") {
                                    UserMessageBubble(message.markdown)
                                }
                            }
                            Role.Assistant -> {
                                item(key = "${message.msgId}-assistant") {
                                    AssistantMessageBubble(message.markdown)
                                }

                            }
                            else -> {}
                        }
                    }

                }
                else{}
            }

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