package com.android.nextai.ui.component.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.icon.SettingsIcon

enum class NoticeType {
    INFO,
    SUCCESS,
    ERROR
}

data class NoticeBubbleData(
    val message: String,
    val type: NoticeType
)

@Composable
fun NoticeBubble(
    visible: Boolean,
    data: NoticeBubbleData?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val (bg, fg, icon) = when (data?.type) {
        NoticeType.ERROR -> Triple(
            colorScheme.errorContainer,
            colorScheme.onErrorContainer,
            colorScheme.error
        )

        else -> Triple(
            colorScheme.primaryContainer,
            colorScheme.onPrimaryFixed,
            colorScheme.onPrimaryFixed
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 96.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = bg,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = when (data?.type) {
                        NoticeType.SUCCESS -> SettingsIcon.Success
                        NoticeType.ERROR -> SettingsIcon.Error
                        else -> SettingsIcon.Info
                    },
                    contentDescription = null,
                    tint = icon
                )

                Text(
                    text = data?.message.orEmpty(),
                    color = fg,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}