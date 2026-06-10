package com.android.nextai.domain.remote.utils

internal enum class HoldState {
    NONE,
    CODE,
    TITLE,
    SPACE,
    BULLET,
    DIVIDER,
    QUOTE,
    NUMBER
}

class StreamBuffer {

    val holdBuffer = StringBuilder()
    private var state = HoldState.NONE

    fun process(char: Char): String? {

        when (state) {

            HoldState.NONE -> {
                return when {
                    char.isDigit() ->
                        switch(HoldState.NUMBER, char)
                    char == '`' -> switch(HoldState.CODE, char)
                    char == '#' -> switch(HoldState.TITLE, char)
                    char == ' ' -> switch(HoldState.SPACE, char)
                    char in listOf('-', '*', '+') -> switch(HoldState.BULLET, char)
                    char == '_' -> switch(HoldState.DIVIDER, char)
                    char == '>' -> switch(HoldState.QUOTE, char)
                    else -> char.toString()
                }
            }

            else -> {
                holdBuffer.append(char)

                if (shouldFlush(char)) {
                    val result = holdBuffer.toString()
                    reset()
                    return result
                }
            }
        }

        return null
    }

    private fun switch(newState: HoldState, char: Char): String? {
        state = newState
        holdBuffer.append(char)
        return null
    }

    /**
     * Determine whether the cache can be flushed.
     */
    private fun shouldFlush(char: Char): Boolean {
        return when (state) {

            HoldState.CODE -> {
                char == '\n' || holdBuffer.endsWith("```")
            }

            HoldState.TITLE -> {
                char != '#'
            }

            HoldState.SPACE -> {
                char != ' '
            }

            HoldState.QUOTE -> {
                char == ' ' || holdBuffer.length > 3
            }

            HoldState.BULLET -> {
                holdBuffer.length > 3
            }

            HoldState.DIVIDER -> {
                holdBuffer.length > 4
            }

            HoldState.NUMBER -> {
                val nonWhitespaceCount = holdBuffer.count { !it.isWhitespace() }
                nonWhitespaceCount >= 2 && !char.isDigit()
            }
            else -> true
        }
    }

    private fun reset() {
        state = HoldState.NONE
        holdBuffer.clear()
    }
}