package com.android.nextai.domain.remote.qianwen

import android.util.Log
import com.android.nextai.domain.remote.AIModelDataSource
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Message
import com.android.nextai.viewmodel.chat.entity.Role
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionStreamOptions
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam


object QianwenRemoteDataSource : AIModelDataSource {
    private const val TAG = "QianwenRemoteDataSource"
    private const val BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    private const val API_KEY = "sk-434e2e9bf2a0432c8dced58f3146bcd2"
    private const val MODEL = "qwen3-max-2026-01-23"

    val openAIClient by lazy {
        OpenAIOkHttpClient.builder()
            .apiKey(API_KEY)
            .baseUrl(BASE_URL)
            .build()
    }

    override suspend fun getAIAnswer(messageList: List<Message>): String {

        val systemMessage = ChatCompletionSystemMessageParam.builder()
            .content("解答用户问题")
            .build()

        val paramsBuilder = ChatCompletionCreateParams.builder()
            .model(MODEL)
            .addMessage(systemMessage)

        messageList.forEach {
            when (it.role) {
                Role.User -> {
                    val userMessage = ChatCompletionUserMessageParam.builder()
                        .content(it.content)
                        .build()
                    paramsBuilder.addMessage(userMessage)
                }

                Role.Assistant -> {
                    val assistantMessage = ChatCompletionAssistantMessageParam.builder()
                        .content(it.content)
                        .build()
                    paramsBuilder.addMessage(assistantMessage)
                }

                else -> {}
            }
        }
        val params = paramsBuilder.build()

        try {
            val create = openAIClient.chat().completions().create(params)
            val choices = create.choices()
            choices.forEach {
                return it.message().content().get()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAIAnswer# error:${e}")
        }
        return ""
    }


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


    override suspend fun getAIStreamingAnswer(
        messageList: List<Message>,
        callback: (GenerationEvent) -> Unit,
    ) {
        val systemMessage = ChatCompletionSystemMessageParam.builder()
            .content("解答用户问题")
            .build()

        val streamOptions = ChatCompletionStreamOptions.builder()
            .includeUsage(true)
            .build()

        val paramsBuilder = ChatCompletionCreateParams.builder()
            .model(MODEL)
            .addMessage(systemMessage)
            .streamOptions(streamOptions)

        var i = 0
        var content = ""
        while (i < messageList.size) {
            val message = messageList[i]
            if (message.role == Role.Assistant) {
                content += message.markdown?.getContent()
                i += 1
                continue
            }
            if (content.isNotEmpty()) {
                val assistantMessage = ChatCompletionAssistantMessageParam.builder()
                    .content(content)
                    .build()
                paramsBuilder.addMessage(assistantMessage)
                content = ""
            }
            if (message.role == Role.User) {
                val userMessage = ChatCompletionUserMessageParam.builder()
                    .content(message.markdown!!.getContent())
                    .build()
                paramsBuilder.addMessage(userMessage)
            }
            i += 1
        }
        val params = paramsBuilder.build()
        try {
            // 1. get Flow<ChatCompletionChunk> by createStreaming
            val streamFlow = openAIClient.chat().completions().createStreaming(params)

            // 2. collect flow and send to callback
            streamFlow.stream().forEach { chunk ->
                if(chunk.choices().isNotEmpty()){
                    val content = chunk.choices().first().delta().content().get()
                    stringBuffer.append(content)
                    val currentTotalLength = stringBuffer.length

                    if (currentTotalLength > sentLength) {
                        val newPart = stringBuffer.substring(sentLength, currentTotalLength)

                        for (char in newPart) {
                            if (isHoldingCode || isHoldingTitle || isHoldingSpace || isHoldingBulletPoint) {
                                // wait for condition to stop hold
                                holdBuffer.append(char)
                                if (isHoldingCode && char == '\n') {
                                    isHoldingCode = false
                                } else if (isHoldingTitle && char != '#') {
                                    isHoldingTitle = false
                                } else if (isHoldingSpace && char != ' ') {
                                    isHoldingSpace = false
                                } else if(isHoldingBulletPoint && holdBuffer.trimStart().length > 3){
                                    isHoldingBulletPoint = false
                                }
                                if(!isHoldingCode && !isHoldingTitle && !isHoldingSpace && !isHoldingBulletPoint){
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
                                    '-' ->{
                                        isHoldingBulletPoint = true
                                        holdBuffer.append(char)
                                    }
                                    else -> {
                                        sentLength += 1
                                        callback(GenerationEvent.Word(char.toString()))

                                    }
                                }
                            }
                        }
                        holdBuffer.clear()
                    }

                }else if(!chunk.usage().isEmpty){
                    if (isHoldingCode && holdBuffer.isNotEmpty()) {
                        val contentToEmit = holdBuffer.toString()
                        callback(GenerationEvent.Word(contentToEmit))
                        holdBuffer.clear()
                        isHoldingCode = false
                    }
                    Log.i(TAG, stringBuffer.toString())
                    callback(GenerationEvent.Done)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAIStreamingAnswer# error:${e}")
        }

    }

}
