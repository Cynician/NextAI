package com.android.nextai.ui.component.markdown.mdnodeview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.LatexAutoWrap
import com.hrm.latex.renderer.model.LatexConfig

@Composable
fun InlineMathView(
    latex: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current, // 自动跟随 Compose 的当前文本颜色
    style: TextStyle = LocalTextStyle.current   // 自动跟随当前的文本样式（如字号等）
) {
    val cleanLatex = latex.removePrefix("$").removeSuffix("$")
        .replace("\\|", "|") // 把前面转义的 \| 恢复回数学意义的 |
    Latex(
        latex = cleanLatex,
        modifier = Modifier.fillMaxWidth(),
        config = LatexConfig(style.fontSize, color = color)
    )
}