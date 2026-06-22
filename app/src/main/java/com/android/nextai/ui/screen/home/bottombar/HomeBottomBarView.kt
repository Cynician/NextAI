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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.android.nextai.ui.Standard
import com.android.nextai.ui.icon.HomeIcon
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.state.ChatSessionState
import com.android.nextai.viewmodel.provider.ProviderViewModel

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
internal fun HomeBottomBar(
    chatViewModel: ChatViewModel,
    providerViewModel: ProviderViewModel,
    modifier: Modifier = Modifier,
    onHeightChanged: (Dp) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val provider by providerViewModel.defaultProvider.collectAsState(null)
    val currentSessionState by chatViewModel.currentSessionState.collectAsState()
    val state = currentSessionState ?: ChatSessionState(sessionId = -1L)
    val isGenerating = state.isGenerating
    val currentSessionId = state.sessionId
    var query by remember { mutableStateOf("") }

    /**
     * `sessionId` == -1L means a new session
     */
    val canSend = query.isNotBlank()
                && provider != null
                && (currentSessionId == -1L || chatViewModel.generationJobs[currentSessionId]?.isActive != true)
    val density = LocalDensity.current
    // Floating capsule bar (ChatGPT-style), modeled after BatchActionBar.
    //
    // This composable is rendered as an overlay inside the Scaffold content (see
    // HomeScreen), aligned to BottomCenter. The outer container only adds
    // navigationBarsPadding + horizontal/bottom spacing and draws NO background, so
    // the area around the capsule is fully transparent and the body shows through.
    //
    // Its measured height (including nav inset) is reported via onHeightChanged so the
    // body can reserve a matching bottom content padding dynamically — this keeps the
    // last message visible even when the bar grows due to multi-line text input.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp)
            .onGloballyPositioned { coordinates ->
                with(density) {
                    onHeightChanged(coordinates.size.height.toDp())
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Standard.SEND_ICON_SIZE, max = 200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp)
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 2 * Standard.SEND_ICON_SIZE + 2.dp)
                    .padding(vertical = 4.dp)
                    .align(Alignment.CenterStart),
                enabled = true,
                singleLine = false,
                textStyle = (LocalTextStyle.current).merge(
                    TextStyle(color = MaterialTheme.colorScheme.onSurface)
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = remember { MutableInteractionSource() },
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .padding(start = 12.dp)
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "问问 NextAI",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField()
                    }
                }
            )
            ActionProgressButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                isGenerating = isGenerating,
                onClickListener = {
                    if (isGenerating) {
                        if (currentSessionId != -1L) {
                            chatViewModel.stopStreamingGen(currentSessionId)
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
    val icon = if (!isGenerating) HomeIcon.Send else HomeIcon.Stop
    val contentDescription = if (isGenerating) "Generating" else "Stop"
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {

        FilledIconButton(
            onClick = { onClickListener() },
            colors = colors,
            shape = shape,
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
                        .size(Standard.SEND_ICON_SIZE)
                )
            }
        }
    }
}