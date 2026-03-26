package com.android.nextai.ui.screen.home.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

object UiUtils {
    private const val TAG = "UiUtils"
    suspend fun LazyListState.scrollToTargetBubble(
        bubbleIndex: Int,
        promptLength: Int,
        thresholdLength: Int = 200
    ) {
        // wait for data ready
        snapshotFlow { this.layoutInfo.totalItemsCount }
            .first { it > bubbleIndex }

        val viewportHeight = this.layoutInfo.viewportSize.height
        if (viewportHeight == 0) return

        var targetIndex = bubbleIndex
        var offset = 0

        if (promptLength > thresholdLength) {
            offset = (viewportHeight * 0.3f).toInt()
            targetIndex += 1
        }
        val finalIndex = minOf(targetIndex, this.layoutInfo.totalItemsCount - 1)
        if (finalIndex >= 0) {
            this.animateScrollToItem(index = finalIndex, scrollOffset = -offset)
        }
    }
}