package com.android.nextai.ui.component.markdown.entity

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.unit.Dp
import com.hrm.latex.renderer.measure.LatexMeasurerState

/**
 * Configuration parameters for LaTeX render.
 *
 * maxTableCellContentWidth: Limit the width of table cell content.
 */
data class LatexRenderParams(
    val latexMeasurer: LatexMeasurerState,
    val inlineContentMap: MutableMap<String, InlineTextContent>,
    val maxTableCellContentWidth: Dp? = null
)