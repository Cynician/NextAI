package com.android.nextai.domain.remote.test


import android.util.Log
import com.android.nextai.domain.remote.AIModelDataSource
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


object TestRemoteDataSource : AIModelDataSource {
    private const val TAG = "TestRemoteDataSource"


    override suspend fun getAIAnswer(messageList: List<Message>): String {
        return TestData.getData()
    }

    private var generationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    var stringBuffer: StringBuilder = StringBuilder("")
    @Volatile
    var sentLength = 0
    @Volatile
    var holdBuffer = StringBuilder() // cache for special string
    @Volatile
    var isHoldingTitle= false
    @Volatile
    var isHoldingCode = false
    @Volatile
    var isHoldingSpace = false
    @Volatile
    var isHoldingBulletPoint= false
    @Volatile
    var isHoldingDivider = false
    @Volatile
    var isHoldingQuote = false


    override suspend fun getAIStreamingAnswer(
        messageList: List<Message>,
        callback: (GenerationEvent) -> Unit,
    ) {
        val testData = TestData.getData()

        try {
            val chunks = mutableListOf<String>()
            var currentIndex = 0

            while (currentIndex < testData.length) {
                // 生成1-20之间的随机大小，但不超过剩余字符数
                val remainingLength = testData.length - currentIndex
                val chunkSize = Random.nextInt(1, minOf(21, remainingLength + 1))

                val chunk = testData.substring(currentIndex, currentIndex + chunkSize)
                chunks.add(chunk)
                currentIndex += chunkSize
            }
            generationScope.launch {
                chunks.forEach { chunk ->
                    delay(Random.nextLong(50, 200))
                    stringBuffer.append(chunk)
                    val currentTotalLength = stringBuffer.length

                    if (currentTotalLength > sentLength) {
                        val newPart = stringBuffer.substring(sentLength, currentTotalLength)
                        for (char in newPart) {
                            if (isHoldingCode || isHoldingTitle || isHoldingSpace || isHoldingBulletPoint || isHoldingDivider || isHoldingQuote) {
                                // wait for condition to stop hold
                                holdBuffer.append(char)
                                if (isHoldingCode && char == '\n' || isHoldingCode && char != '`' && holdBuffer.length<=3) {
                                    isHoldingCode = false
                                } else if (isHoldingTitle && char != '#') {
                                    isHoldingTitle = false
                                } else if (isHoldingSpace && char != ' ' && char !='-' && char != '*' && char!='+') {
                                    isHoldingSpace = false
                                } else if (isHoldingBulletPoint || isHoldingDivider) {
                                    val isBulletPointRegex = Regex("""^(\s*)([-*+])\s+.*$""")
                                    if(isHoldingBulletPoint && holdBuffer.length > 3 && !holdBuffer.matches(isBulletPointRegex)||
                                        isHoldingBulletPoint && holdBuffer.matches(isBulletPointRegex))
                                    {
                                        isHoldingBulletPoint = false
                                        isHoldingDivider = false
                                    }
                                    if(isHoldingDivider && holdBuffer[0]=='-'&& holdBuffer.toString().count { it == '-' } != holdBuffer.length && holdBuffer.length <= 3||
                                        isHoldingDivider && holdBuffer[0]=='*'&& holdBuffer.toString().count { it == '*' } != holdBuffer.length && holdBuffer.length <= 3||
                                        isHoldingDivider &&  holdBuffer[0]=='_'&& holdBuffer.toString().count { it == '_' } != holdBuffer.length && holdBuffer.length <= 3 ||
                                        isHoldingDivider && holdBuffer.length>4)
                                    {
                                        isHoldingDivider = false
                                    }
                                }else if(isHoldingQuote && char == ' ' || isHoldingQuote && holdBuffer.length > 3){
                                    isHoldingQuote = false
                                }
                                if(char == '\n'){
                                    isHoldingCode = false
                                    isHoldingTitle = false
                                    isHoldingSpace = false
                                    isHoldingBulletPoint = false
                                    isHoldingDivider = false
                                    isHoldingQuote = false
                                }
                                if (!isHoldingCode &&
                                    !isHoldingTitle &&
                                    !isHoldingSpace &&
                                    !isHoldingBulletPoint &&
                                    !isHoldingDivider &&
                                    !isHoldingQuote)
                                {
                                    val contentToEmit = holdBuffer.toString()
                                    sentLength += contentToEmit.length
                                    callback(GenerationEvent.Word(contentToEmit))
                                    // reset hold buffer
                                    holdBuffer.clear()
                                }
                            } else {
                                when (char) {
                                    '`' -> {
                                        isHoldingCode = true
                                        holdBuffer.append(char)
                                    }

                                    '#' -> {
                                        isHoldingTitle = true
                                        holdBuffer.append(char)
                                    }

                                    ' ' -> {
                                        isHoldingSpace = true
                                        holdBuffer.append(char)
                                    }

                                    '-', '*' ,'+' -> {
                                        isHoldingBulletPoint = true
                                        if(char=='-') isHoldingDivider = true
                                        holdBuffer.append(char)
                                    }
                                    '_' ->{
                                        isHoldingDivider = true
                                        holdBuffer.append(char)
                                    }
                                    '>' -> {
                                        isHoldingQuote = true
                                        holdBuffer.append(char)
                                    }
                                    else -> {
                                        sentLength += 1
                                        delay(20)
                                        callback(GenerationEvent.Word(char.toString()))
                                    }

                                }
                            }
//                            sentLength += 1
//                            delay(20)
//                            callback(GenerationEvent.Word(char.toString()))
                        }
                        holdBuffer.clear()
                    }

                }

                if (isHoldingCode && holdBuffer.isNotEmpty()) {
                    val contentToEmit = holdBuffer.toString()
                    callback(GenerationEvent.Word(contentToEmit))
                    holdBuffer.clear()
                    isHoldingCode = false
                }
                Log.i(TAG, stringBuffer.toString())
                callback(GenerationEvent.Done)
            }

        } catch (e: Exception) {
            Log.e(TAG, "getAIStreamingAnswer# error:${e}")
        }

    }

}
