package com.android.nextai.ui.component.loading

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingDots(
    dotsColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500, easing = FastOutSlowInEasing
            ), repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(alpha1)
                    .background(dotsColor, CircleShape)
            )
        }
    }
}