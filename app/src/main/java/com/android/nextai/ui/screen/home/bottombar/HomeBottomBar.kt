package com.android.nextai.ui.screen.home.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.ActionButton
import com.android.nextai.ui.icon.AppIcon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeBottomBar() {
    val context = LocalContext.current
    var value by remember { mutableStateOf("") }

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
                .padding(horizontal = Standard.SpacingSm)
                .padding(top = Standard.SpacingSm, bottom = 10.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 56.dp),
                    placeholder = {
                        Text(text = "Ask NextAI…")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                ActionButton(
                    onClickListener = { null },
                    icon = AppIcon.Send,
                    shape = MaterialShapes.Ghostish.toShape(),
                    modifier = Modifier
                        .padding(end = Standard.SpacingMd)
                        .align(Alignment.BottomEnd),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(0.3f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}