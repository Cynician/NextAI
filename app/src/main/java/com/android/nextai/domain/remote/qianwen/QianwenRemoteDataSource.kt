package com.android.nextai.domain.remote.qianwen

import android.util.Log
import com.android.nextai.domain.remote.AIModelDataSource
import com.android.nextai.viewmodel.chat.ChatViewModel
import com.android.nextai.viewmodel.chat.entity.Message
import com.android.nextai.viewmodel.chat.entity.Role
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionToolMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam

import javax.inject.Inject

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

}
