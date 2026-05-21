package com.android.nextai.ui.component.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.icon.SettingsIcon

@Composable
fun SuccessTipLabel(
    visible: Boolean,
    text: String = "",
) {
    AnimatedVisibility(visible = visible) {
        Row(
            modifier = Modifier
                .background(
                    color = Color(0xFFDBF1E7),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Icon(
                imageVector = SettingsIcon.CheckCircle,
                contentDescription = null,
                tint = Color.Unspecified,

                )
            Text(
                text = text,
                color = Color(0xFF17B26A),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}