package com.android.nextai.ui.component.loading

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun SequentialJumpingDots(
    dotsColor: Color = MaterialTheme.colorScheme.primary
) {

    val dotCount = 4
    val delay = 140
    val jumpDuration = 320

    val totalDuration =
        delay * dotCount + jumpDuration

    val transition =
        rememberInfiniteTransition(label = "jump")

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        repeat(dotCount) { index ->

            val start = index * delay

            val offsetY by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {

                        durationMillis = totalDuration
                        // Static
                        0f at 0
                        // Jump
                        0f at start
                        // The highest point
                        -20f at (start + jumpDuration / 2) using FastOutSlowInEasing
                        // Fall
                        0f at (start + jumpDuration)
                    }
                ),
                label = "offset"
            )

            Box(
                modifier = Modifier
                    .size(3.dp)
                    .graphicsLayer {
                        translationY = offsetY
                    }
                    .background(
                        dotsColor,
                        CircleShape
                    )
            )
        }
    }
}