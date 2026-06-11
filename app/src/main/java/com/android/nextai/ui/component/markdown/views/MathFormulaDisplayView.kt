package com.android.nextai.ui.component.markdown.views

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import com.hrm.latex.renderer.model.LatexConfig

@Composable
fun MathBlockView(
    formula: String,
    style: TextStyle = LocalTextStyle.current,
) {
    val latexMeasurer = rememberLatexMeasurer()
    val dimensions = latexMeasurer.measure(latex = formula, config = LatexConfig(fontSize = style.fontSize))

    if (dimensions != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Latex(
                    latex = formula,
                    config = LatexConfig(fontSize = style.fontSize)
                )
            }
        }
    } else {
        Text(
            text = formula,
            style = style,
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 4.dp)
        )
    }
}