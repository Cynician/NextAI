package com.android.nextai.ui.component.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.markdown.category.BlockQuoteView
import com.android.nextai.ui.component.markdown.category.BodyTextView
import com.android.nextai.ui.component.markdown.category.BulletPointView
import com.android.nextai.ui.component.markdown.category.CodeBlockView
import com.android.nextai.ui.component.markdown.category.HeadingTextView
import com.android.nextai.ui.component.markdown.category.InlineCodeView
import com.android.nextai.ui.component.markdown.category.InlineMathView
import com.android.nextai.ui.component.markdown.category.MathBlockView
import com.android.nextai.ui.component.markdown.category.NumberedPointView
import com.android.nextai.ui.component.markdown.category.TableView
import com.android.nextai.ui.component.markdown.entity.InlineColors
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.component.markdown.entity.bottomSpacing
import com.android.nextai.ui.component.markdown.entity.topSpacing


@Composable
fun MarkdownElementView(element: MarkdownElement?, colors: InlineColors) {
    when (element) {
        is MarkdownElement.Heading1 -> HeadingTextView(
            element.text,
            colors,
            24.sp,
            FontWeight.Bold,
            4.dp
        )

        is MarkdownElement.Heading2 -> HeadingTextView(
            element.text,
            colors,
            20.sp,
            FontWeight.SemiBold,
            3.dp
        )

        is MarkdownElement.Heading3 -> HeadingTextView(
            element.text,
            colors,
            17.sp,
            FontWeight.SemiBold,
            2.dp
        )

        is MarkdownElement.Heading4 -> HeadingTextView(
            element.text,
            colors,
            15.sp,
            FontWeight.Medium,
            2.dp
        )

        is MarkdownElement.Heading5 -> HeadingTextView(
            element.text,
            colors,
            14.sp,
            FontWeight.Medium,
            1.dp
        )

        is MarkdownElement.Heading6 -> HeadingTextView(
            element.text,
            colors,
            13.sp,
            FontWeight.Medium,
            1.dp,
            0.87f
        )

        is MarkdownElement.Body -> BodyTextView(element.text, colors)
        is MarkdownElement.BulletPoint -> BulletPointView(element.text, element._level, colors)
        is MarkdownElement.NumberedPoint -> NumberedPointView(element.text, element.number, colors)
        is MarkdownElement.Quote -> BlockQuoteView(element.text, element._level, colors)
        is MarkdownElement.CodeBlock -> CodeBlockView(element.code, element.language, element.enableHighlightCode)
        is MarkdownElement.InlineCode -> InlineCodeView(element.text)
        is MarkdownElement.Table -> TableView(
            element.headers,
            element.rows,
            element.alignments,
            colors
        )

        is MarkdownElement.MathBlock -> MathBlockView(element.expression, element.isTypst)
        is MarkdownElement.InlineMath -> InlineMathView(element.expression)
        is MarkdownElement.Divider -> HorizontalDivider(
            modifier = Modifier.padding(vertical = Standard.SpacingXs),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        else -> {
            null
        }
    }
}


/**
 * Lazy version — each markdown element is a separate LazyList item.
 * Only visible items are composed. Use inside a LazyColumn.
 * Element-aware spacing: headings get more top padding, blocks get breathing room.
 */
fun LazyListScope.lazyMarkdownItems(
    elements: List<MarkdownElement>,
    keyPrefix: Long,
    modifier: Modifier = Modifier,
) {
    items(
        count = elements.size,
        key = { index -> "${keyPrefix}-md-$index" },
        contentType = { index -> elements[index]::class.simpleName }
    ) { index ->
        val element = elements[index]
        val scheme = MaterialTheme.colorScheme
        val colors = remember(scheme) {
            InlineColors(
                codeBg = scheme.surfaceVariant.copy(alpha = 0.5f),
                highlightBg = scheme.primary.copy(alpha = 0.3f),
                mathColor = scheme.primary
            )
        }
        SelectionContainer {
            Box(
                modifier = modifier.padding(
                    top = element.topSpacing(),
                    bottom = element.bottomSpacing()
                )
            ) {
                MarkdownElementView(element, colors)
            }
        }
    }
}





