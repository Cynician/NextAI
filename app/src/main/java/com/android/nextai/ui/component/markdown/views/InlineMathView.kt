package com.android.nextai.ui.component.markdown.views

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig

@Composable
fun InlineMathView(
    isFormulaTooLong: Boolean,
    formula: String,
    style: TextStyle = LocalTextStyle.current
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .let { modifier ->
                if (isFormulaTooLong) {
                    modifier.horizontalScroll(rememberScrollState())
                } else {
                    modifier
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Latex(
            latex = formula,
            config = LatexConfig(fontSize = style.fontSize)
        )
    }
}