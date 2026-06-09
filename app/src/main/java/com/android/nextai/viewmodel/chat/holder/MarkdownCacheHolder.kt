package com.android.nextai.viewmodel.chat.holder

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.android.nextai.ui.component.markdown.parser.MarkdownIncrementalParser
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownCacheHolder @Inject constructor() {

    companion object {
        private const val TAG = "MarkdownStateHolder"
        // LRU cache size
        private const val MAX_CACHE_SIZE = 200
    }

    private val parserMap = mutableStateMapOf<Long, MarkdownIncrementalParser>()

    /**
     * LRU
     *
     * accessOrder = true:
     * When get/put, it will automatically move to the back of the line
     */
    private val lruMap = object : LinkedHashMap<Long, Unit>(
        MAX_CACHE_SIZE,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<Long, Unit>?
        ): Boolean {
            val shouldRemove = size > MAX_CACHE_SIZE
            if (shouldRemove && eldest != null) {
                parserMap.remove(eldest.key)
                Log.d(TAG, "LRU activate, remove: ${eldest.key}")
            }
            return shouldRemove
        }
    }

    @Synchronized
    fun get(
        messageId: Long
    ): MarkdownIncrementalParser? {
        val parser = parserMap[messageId]
        if (parser != null) {
            // Update LRU
            lruMap[messageId] = Unit
        }
        return parser
    }

    @Synchronized
    fun getOrCreate(
        messageId: Long
    ): MarkdownIncrementalParser {

        // Already exists
        parserMap[messageId]?.let { parser ->
            // Update LRU
            lruMap[messageId] = Unit
            return parser
        }

        // Create new parser
        val parser = MarkdownIncrementalParser()

        parserMap[messageId] = parser
        // Insert LRU
        lruMap[messageId] = Unit
        return parser
    }

    fun size(): Int {
        return parserMap.size
    }
}