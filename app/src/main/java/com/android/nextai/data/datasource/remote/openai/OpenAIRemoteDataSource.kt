package com.android.nextai.data.datasource.remote.openai

import android.util.Log
import com.android.nextai.data.datasource.remote.AIDataSource
import com.android.nextai.data.datasource.remote.utils.OpenAIClientPool
import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.MessageType
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionStreamOptions
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive


object OpenAIRemoteDataSource : AIDataSource {

    private const val TAG = "OpenAIRemoteDataSource"

    override suspend fun getAIStreamingAnswer(
        messageList: List<Message>,
        provider: Provider,
        modelId: String,
        generationCallback: (GenerationEvent) -> Unit,
    ) {
        try {
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
                .model(modelId)
                .addMessage(sysMessage)
                .streamOptions(streamOptions)

            messageList.forEach { message ->
                when (message.type) {
                    MessageType.ASSISTANT -> {
                        val assistantMessage = ChatCompletionMessageParam.ofAssistant(
                            ChatCompletionAssistantMessageParam.builder()
                                .content(message.content)
                                .build()
                        )
                        paramsBuilder.addMessage(assistantMessage)
                    }

                    MessageType.USER -> {
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
                for (chunk in flow.stream().iterator()) {

                    // External coroutine terminates and cancels this task.
                    currentCoroutineContext().ensureActive()

                    Log.d(TAG, "chunk=$chunk")
                    chunk.choices().firstOrNull()?.also { choice ->
                        choice.delta()
                            .content()
                            .ifPresent {
                                generationCallback(GenerationEvent.Chunk(it))
                            }
                        choice.finishReason().ifPresent {
                            Log.d(TAG, "done=$it")
                            generationCallback(GenerationEvent.Done)
                        }
                    }
                }
            }
            Log.d(TAG, "getAIStreamingAnswer# streaming finished")
        } catch (e: Exception) {
            Log.e(TAG, "getAIStreamingAnswer# ${e.message}", e)
            generationCallback(GenerationEvent.Error(e.message.toString()))
        }
    }
}