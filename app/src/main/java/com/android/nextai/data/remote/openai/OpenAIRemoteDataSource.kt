package com.android.nextai.data.remote.openai

import android.util.Log
import com.android.nextai.data.remote.AIModelDataSource
import com.android.nextai.domain.model.GenerationEvent
import com.android.nextai.data.remote.utils.OpenAIClientPool
import com.android.nextai.data.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datebase.room.entity.MessageEntity
import com.android.nextai.domain.model.Role
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageParam
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
        try {

            val model = provider.models.firstOrNull()?.id?:throw Exception("model is null")

            val openAIClient = OpenAIClientPool.getClient(provider.apiUrl, provider.apiKey)

            val sysMessage = ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder()
                    .content("解答用户问题")
                    .build()
            )

            val streamOptions = ChatCompletionStreamOptions.builder()
                .includeUsage(true)
                .build()

            val paramsBuilder = ChatCompletionCreateParams.builder()
                .model(model)
                .addMessage(sysMessage)
                .streamOptions(streamOptions)

            messageList.forEach { message ->
                when (message.role) {
                    Role.Assistant.name -> {
                        val assistantMessage = ChatCompletionMessageParam.ofAssistant(
                            ChatCompletionAssistantMessageParam.builder()
                                .content(message.content)
                                .build()
                        )
                        paramsBuilder.addMessage(assistantMessage)
                    }

                    Role.User.name -> {
                        val userMessage = ChatCompletionMessageParam.ofUser(
                            ChatCompletionUserMessageParam.builder()
                                .content(message.content)
                                .build()
                        )
                        paramsBuilder.addMessage(userMessage)
                    }
                }
            }

            val params = paramsBuilder.build()


            // 1. get Flow<ChatCompletionChunk> by createStreaming
            val streamFlow = openAIClient.chat().completions().createStreaming(params)

            // 2. collect flow and send to callback
            streamFlow.use { flow ->
                flow.stream().forEach { chunk ->
                    Log.d(TAG, "chunk=$chunk")
                    chunk.choices().firstOrNull()?.also { choice ->
                        choice.delta()
                            .content()
                            .ifPresent {
                                callback(GenerationEvent.Word(it))
                            }
                        choice.finishReason().ifPresent {
                            Log.d(TAG, "done=$it")
                            callback(GenerationEvent.Done)
                        }
                    }
                }
            }
            Log.d(TAG, "getAIStreamingAnswer# streaming finished")
        } catch (e: Exception) {
            Log.e(TAG, "getAIStreamingAnswer# error:${e.message}", e)
            callback(GenerationEvent.Error(e.message.toString()))
        }
    }
}
