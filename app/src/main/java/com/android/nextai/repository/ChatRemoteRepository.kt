package com.android.nextai.repository

import android.util.Log
import com.android.nextai.data.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datebase.room.entity.MessageEntity
import com.android.nextai.data.remote.AIFactory
import com.android.nextai.data.remote.ApiType
import com.android.nextai.domain.model.GenerationEvent
import com.android.nextai.data.remote.utils.StreamBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.text.iterator

class ChatRemoteRepository @Inject constructor() {

    companion object {
        private const val TAG = "ChatRemoteRepository"
    }

    suspend fun getAIAnswer(apiType: ApiType, messageList: List<MessageEntity>): String =
        withContext(Dispatchers.IO) {
            return@withContext AIFactory.createAIModel(apiType).getAIAnswer(messageList)
        }

    suspend fun getAIStreamingAnswer(
        apiType: ApiType,
        messageList: List<MessageEntity>,
        provider: ProviderEntity,
        callback: (GenerationEvent) -> Unit,
    ) = withContext(Dispatchers.IO) {
        AIFactory.createAIModel(apiType).getAIStreamingAnswer(messageList, provider, callback)
    }

    fun streamingGen(
        apiType: ApiType,
        messageList: List<MessageEntity>,
        provider: ProviderEntity,
    ): Flow<GenerationEvent> =
        callbackFlow<GenerationEvent> {
            val mdBuffer = StreamBuffer()
            val callback: (GenerationEvent) -> Unit = { event ->
                when (event) {
                    is GenerationEvent.Word -> {
                        for (char in event.content) {
                            val out = mdBuffer.process(char)
                            if (out != null) {
                                trySend(GenerationEvent.Word(out))
                            }
                        }
                    }

                    is GenerationEvent.Done,
                    is GenerationEvent.Error,
                        -> {
                        if (mdBuffer.holdBuffer.isNotEmpty()) {
                            trySend(GenerationEvent.Word(mdBuffer.holdBuffer.toString()))
                            mdBuffer.holdBuffer.clear()
                        }
                        trySend(event)
                        close()
                    }
                }
            }
            try {
                Log.d(TAG, "[streamingGen] start to streaming...")
                getAIStreamingAnswer(apiType, messageList, provider, callback)
            } catch (e: Exception) {
                trySend(GenerationEvent.Error("fail to streaming：${e.message}"))
                close()
            }
            awaitClose {
                Log.d(TAG, "[streamingGen] close...")
            }
        }.buffer(Channel.UNLIMITED)
            .onEach { delay(3) }
            .flowOn(Dispatchers.IO)
}