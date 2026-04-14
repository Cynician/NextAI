package com.android.nextai.domain.remote

class StreamBuffer {

    private val holdBuffer = StringBuilder()
    private var state = HoldState.NONE

    fun process(char: Char): String? {

        when (state) {

            HoldState.NONE -> {
                return when (char) {
                    '`' -> switch(HoldState.CODE, char)
                    '#' -> switch(HoldState.TITLE, char)
                    ' ' -> switch(HoldState.SPACE, char)
                    '-', '*', '+' -> switch(HoldState.BULLET, char)
                    '_' -> switch(HoldState.DIVIDER, char)
                    '>' -> switch(HoldState.QUOTE, char)
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
     * 判断是否可以释放缓存
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

            else -> true
        }
    }

    private fun reset() {
        state = HoldState.NONE
        holdBuffer.clear()
    }

    enum class HoldState {
        NONE,
        CODE,
        TITLE,
        SPACE,
        BULLET,
        DIVIDER,
        QUOTE
    }
}