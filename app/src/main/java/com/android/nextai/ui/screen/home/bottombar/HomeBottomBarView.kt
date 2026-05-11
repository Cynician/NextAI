package com.android.nextai.ui.screen.home.bottombar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.icon.AppIcon
import com.android.nextai.viewmodel.chat.ChatViewModel

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
internal fun HomeBottomBar(
    chatViewModel: ChatViewModel
) {
    var query by remember { mutableStateOf("") }

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
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 10.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 56.dp),
                    placeholder = { Text(text = "Ask NextAI…") },
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
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .align(Alignment.BottomEnd),
                    icon = AppIcon.Send,
                    shape = MaterialTheme.shapes.small,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(0.3f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    onClickListener = {
                        if(query.isNotEmpty() &&
                            (chatViewModel.generationJob == null ||
                                    chatViewModel.generationJob?.isActive == false))
                        {
                            chatViewModel.sendUserQuery(query)
                            query = ""
                        }
                    },
                )
            }
        }
    }
}