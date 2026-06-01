package com.android.nextai.domain.remote.qianwen

import android.util.Log
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.sqlite.entity.MessageEntity
import com.android.nextai.domain.remote.AIModelDataSource
import com.android.nextai.domain.remote.utils.OpenAIClientPool
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Role
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionStreamOptions
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam


object OpenAIRemoteDataSource : AIModelDataSource {

    private const val TAG = "OpenAIRemoteDataSource"

    override suspend fun getAIAnswer(messageList: List<MessageEntity>): String {
        val systemMessage = ChatCompletionSystemMessageParam.builder()
            .content("解答用户问题")
            .build()
        val paramsBuilder = ChatCompletionCreateParams.builder()
            .model("")
            .addMessage(systemMessage)
        messageList.forEach {
            when (it.role) {
                Role.User.name -> {
                    val userMessage = ChatCompletionUserMessageParam.builder()
                        .content(it.content)
                        .build()
                    paramsBuilder.addMessage(userMessage)
                }
                Role.Assistant.name -> {
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
//            val create = openAIClient.chat().completions().create(params)
//            val choices = create.choices()
//            choices.forEach {
//                return it.message().content().get()
//            }
        } catch (e: Exception) {
            Log.e(TAG, "getAIAnswer# error:${e}")
            return ""
        }
        return ""
    }

    override suspend fun getAIStreamingAnswer(
        messageList: List<MessageEntity>,
        provider: ProviderEntity,
        callback: (GenerationEvent) -> Unit,
    ) {
        val apiUrl = provider.apiUrl
        val apiKey = provider.apiKey
        val models = provider.models

        val openAIClient = OpenAIClientPool.getClient(apiUrl, apiKey)
        val systemMessage = ChatCompletionSystemMessageParam.builder()
            .content("解答用户问题")
            .build()
        val streamOptions = ChatCompletionStreamOptions.builder()
            .includeUsage(true)
            .build()
        val paramsBuilder = ChatCompletionCreateParams.builder()
            .model(models.first().id)
            .addMessage(systemMessage)
            .streamOptions(streamOptions)

        var i = 0
        var content = ""
        while (i < messageList.size) {
            val message = messageList[i]
            if (message.role == Role.Assistant.name) {
                content += message.content
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
            if (message.role == Role.User.name) {
                val userMessage = ChatCompletionUserMessageParam.builder()
                    .content(message.content)
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
                chunk.choices().firstOrNull()?.also { choice ->
                    choice.delta()
                        .content()
                        .ifPresent {
                            callback(GenerationEvent.Word(it))
                        }

                    choice.finishReason().ifPresent {
                            callback(GenerationEvent.Done)
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAIStreamingAnswer# error:${e}")
            callback(GenerationEvent.Done)
        }
    }
}
