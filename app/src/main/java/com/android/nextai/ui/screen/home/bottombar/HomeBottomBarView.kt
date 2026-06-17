package com.android.nextai.ui.screen.home.bottombar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.icon.HomeIcon
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.state.ChatSessionState
import com.android.nextai.viewmodel.provider.ProviderViewModel

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
internal fun HomeBottomBar(
    chatViewModel: ChatViewModel,
    providerViewModel: ProviderViewModel,
) {
    val focusManager = LocalFocusManager.current
    val provider by providerViewModel.defaultProvider.collectAsState(null)
    val currentSessionState by chatViewModel.currentSessionState.collectAsState()
    val state = currentSessionState ?: ChatSessionState(sessionId = -1L)
    val isGenerating = state.isGenerating
    val currentSessionId = state.sessionId
    var query by remember { mutableStateOf("") }

    val canSend = query.isNotBlank()
                && provider != null
                && chatViewModel.generationJobs[currentSessionId]?.isActive != true
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary.copy(0.04f)
                    .compositeOver(MaterialTheme.colorScheme.background)
            )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 10.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 56.dp),
                    placeholder = { Text(text = "问问 NextAI") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                ActionProgressButton(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .align(Alignment.BottomEnd),
                    isGenerating = isGenerating,
                    onClickListener = {
                        if (isGenerating) {
                            val sid = currentSessionId
                            if (sid != -1L) {
                                chatViewModel.stopStreamingGen(sid)
                            }
                            return@ActionProgressButton
                        }
                        val currentProvider = provider ?: return@ActionProgressButton
                        if (!canSend) return@ActionProgressButton
                        focusManager.clearFocus()
                        chatViewModel.sendUserQuery(query.trim(), currentProvider)
                        query = ""
                    },
                )
            }
        }
    }
}

@SuppressLint("ModifierParameter")
@Composable
private fun ActionProgressButton(
    onClickListener: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primary.copy(0.2f),
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    val icon = if (!isGenerating) HomeIcon.ArrowUp else HomeIcon.Stop
    val contentDescription = if (isGenerating) "Generating" else "Stop"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(40.dp)
                .visible(isGenerating),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )

        FilledIconButton(
            onClick = { onClickListener() },
            colors = colors,
            shape = shape,
            modifier = Modifier.size(36.dp)
        ) {
            AnimatedContent(
                targetState = icon,
                transitionSpec = {
                    val enterTransition = scaleIn(
                        animationSpec = tween(durationMillis = 300),
                        initialScale = 0.4f
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 300)
                    )

                    val exitTransition = scaleOut(
                        animationSpec = tween(durationMillis = 300),
                        targetScale = 0.4f
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    )

                    enterTransition togetherWith exitTransition
                },
                label = "IconScaleAnimation"
            ) { targetIcon ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}