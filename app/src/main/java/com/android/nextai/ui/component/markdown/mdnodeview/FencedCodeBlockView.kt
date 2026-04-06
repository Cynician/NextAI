package com.android.nextai.ui.component.markdown.mdnodeview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.nextai.ui.Standard
import com.android.nextai.ui.component.button.ActionButton
import com.android.nextai.ui.component.button.ActionToggleButton
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.utils.highlightCode
import com.android.nextai.ui.component.markdown.utils.resolveSyntaxTheme
import com.android.nextai.ui.icon.AppIcon
import com.android.nextai.ui.theme.MapleMonoFontFamily

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

@Composable
fun FencedCodeBlockView(node: MarkdownNode.FencedCodeBlock, isHighlightCode:Boolean = false) {
    var isExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val headerBg = remember(isDark) { if (isDark) Color(0xFF282C34) else Color(0xFFF5F5F5) }
    val headerFg = remember(isDark) { if (isDark) Color(0xFFABB2BF) else Color(0xFF383A42) }
    val lang = node.lang
    val code = node.code
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(headerBg.copy(alpha = 0.85f))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    onClickListener = {},
                    icon = AppIcon.Code,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = headerFg.copy(0.1f),
                        contentColor = headerFg.copy(0.7f)
                    )
                )
                if (lang.isNotEmpty()) {
                    Text(
                        text = lang.uppercase(),
                        fontFamily = MapleMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = headerFg.copy(alpha = 0.5f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Standard.SpacingXs)) {
                ActionButton(
                    onClickListener = {
                        val cm =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(lang, code))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    },
                    icon = AppIcon.Copy,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = headerFg.copy(0.1f),
                        contentColor = headerFg.copy(0.7f)
                    )
                )
                ActionToggleButton(
                    checked = isExpanded,
                    onCheckedChange = { isExpanded = !isExpanded },
                    icon = if (isExpanded) AppIcon.ChevronUp else AppIcon.ChevronDown,
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = headerFg.copy(0.1f),
                        contentColor = headerFg.copy(0.7f),
                        checkedContainerColor = headerFg.copy(0.15f),
                        checkedContentColor = headerFg.copy(0.8f)
                    )
                )
            }
        }

        if (isExpanded) {
            val syntaxTheme = resolveSyntaxTheme()
            val highlighted = remember(code, lang) {
                if (lang.isNotBlank() && isHighlightCode) highlightCode(code, lang, syntaxTheme)
                else null
            }
            HorizontalDivider(color = headerFg.copy(alpha = 0.1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = Standard.SpacingSm)
            ) {
                Text(
                    text = highlighted ?: AnnotatedString(code),
                    fontFamily = MapleMonoFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}