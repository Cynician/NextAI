package com.android.nextai.ui.component.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.icon.SettingsIcon

@Composable
fun SettingPasswordField(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: () -> Unit,
    placeholder: String = "",
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(8.dp),

            textStyle = TextStyle(
                fontSize = 12.sp
            ),

            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 12.sp
                )
            },

            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },

            trailingIcon = {

                IconButton(
                    onClick = onPasswordVisibleChange
                ) {

                    Icon(
                        imageVector = if (passwordVisible) {
                            SettingsIcon.VisibilityOff
                        } else {
                            SettingsIcon.Visibility
                        },
                        contentDescription = null
                    )
                }
            },

            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),

            singleLine = true
        )
    }
}

@Preview
@Composable
fun ShowSettingPasswordField() {
    SettingPasswordField(
        title = "test",
        value = "",
        onValueChange = {},
        passwordVisible = false,
        onPasswordVisibleChange = {},
        placeholder = "",
    )
}