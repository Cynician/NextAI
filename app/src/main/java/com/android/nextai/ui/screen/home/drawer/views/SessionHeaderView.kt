package com.android.nextai.ui.screen.home.drawer.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.domain.database.sqlite.entity.SessionEntity
import com.android.nextai.ui.component.checkbox.CircleCheckbox
import com.android.nextai.ui.icon.HomeIcon

@Composable
fun SessionHeaderView(
    title: String,
    sessions: List<SessionEntity>,
    selectedIdSet: Set<Long>,
    isBatchSelectMode: Boolean,
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(visible = isBatchSelectMode) {
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
            imageVector = HomeIcon.ArrowRight,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
            tint = Color.Unspecified
        )
    }
}