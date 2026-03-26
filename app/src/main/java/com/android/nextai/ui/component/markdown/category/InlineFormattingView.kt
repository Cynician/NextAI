package com.android.nextai.ui.component.markdown.category

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils.buildInlineFormatted

@SuppressLint("ComposableNaming")
@Composable
fun InlineFormattingView(text: String, colors: InlineColors): AnnotatedString =
    remember(text, colors) { buildInlineFormatted(text, colors) }