package com.android.nextai.ui.component.markdown.entity

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors extracted once from MaterialTheme — passed to non-composable formatters
 * to avoid reading theme state inside every Text().
 */
@Immutable
data class InlineColors(
    val codeBg: Color,
    val highlightBg: Color,
    val mathColor: Color,
)
