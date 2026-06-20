package com.android.nextai.ui.screen.home.drawer.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.domain.model.chat.Session
import com.android.nextai.ui.component.checkbox.CircleCheckbox
import com.android.nextai.ui.icon.HomeIcon

@Composable
fun SessionHeaderView(
    title: String,
    sessions: List<Session>,
    selectedIdSet: Set<Long>,
    isBatchSelectMode: Boolean,
    isExpand: Boolean,
    onToggleExpand: () -> Unit,
    onSelectGroup: (Boolean) -> Unit,
) {
    val allSelected = sessions.isNotEmpty() && sessions.all { selectedIdSet.contains(it.id) }
    val rotation by animateFloatAsState(targetValue = if (isExpand) 90f else 0f, label = "arrow_rotation")

    // CircleCheckbox animation
    val transitionProgress by animateFloatAsState(
        targetValue = if (isBatchSelectMode) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "checkbox_transition"
    )

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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = title,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Icon(
            imageVector = HomeIcon.ArrowRight,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(18.dp)
                .graphicsLayer {
                    translationY = (-20f) * (1f - transitionProgress)
                    alpha = transitionProgress
                }
                .visible(isBatchSelectMode),
            contentAlignment = Alignment.Center
        ) {
            CircleCheckbox(
                modifier = Modifier.size(16.dp),
                checked = allSelected,
                onCheckedChange = onSelectGroup
            )
        }
    }
}