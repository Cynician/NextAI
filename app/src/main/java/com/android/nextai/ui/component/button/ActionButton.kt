package com.android.nextai.ui.component.button

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.nextai.ui.Standard

@SuppressLint("ModifierParameter")
@Composable
fun ActionButton(
    onClickListener: () -> Unit,
    icon: ImageVector,
    contentDescription: String = "Description",
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primary.copy(0.06f),
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    FilledIconButton(
        onClick = { onClickListener() },
        colors = colors,
        shape = shape,
        modifier = modifier.size(Standard.ActionIconSize)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            Modifier.padding(Standard.ActionIconPadding)
        )
    }
}



