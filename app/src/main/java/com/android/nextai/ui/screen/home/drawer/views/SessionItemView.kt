package com.android.nextai.ui.screen.home.drawer.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.nextai.domain.database.sqlite.entity.SessionEntity
import com.android.nextai.ui.component.checkbox.CircleCheckbox
import com.android.nextai.ui.icon.HomeIcon

@Composable
fun SessionItemView(
    session: SessionEntity,
    isActive: Boolean,
    isPin: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUnpinClick: () -> Unit,
) {

    val background by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        label = ""
    )

    val borderColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0f)
            },
        label = ""
    )

    // CircleCheckbox&Icon animation
    val transitionProgress by animateFloatAsState(
        targetValue = if (isSelectionMode) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "selection_transition"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(12.dp),
            color = background,
            border = BorderStroke(
                width = if (isSelected) 1.dp else 0.dp,
                color = borderColor
            )
        ) {

            Row(
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = session.title ?: "Untitled",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
        ) {

            // =========================
            // Pin icon
            // =========================

            Icon(
                imageVector = HomeIcon.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        // Slide down
                        translationY = transitionProgress * 24f
                        // Fade out
                        alpha = 1f - transitionProgress
                    }
                    .clickable(
                        enabled = !isSelectionMode,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onUnpinClick()
                    }
                    .visible(isPin)
            )

            // =========================
            // CircleCheckbox
            // =========================

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = (-24f) * (1f - transitionProgress)
                        // Faint in
                        alpha = transitionProgress
                    }
            ) {
                CircleCheckbox(
                    modifier = Modifier.size(20.dp),
                    checked = isSelected,
                    onCheckedChange = {
                        onClick()
                    }
                )
            }
        }
    }
}