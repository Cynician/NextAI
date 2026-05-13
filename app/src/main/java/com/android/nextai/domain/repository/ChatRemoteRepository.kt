package com.android.nextai.domain.repository

import android.util.Log
import com.android.nextai.domain.database.db.entity.MessageEntity
import com.android.nextai.domain.remote.AIFactory
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.StreamBuffer
import com.android.nextai.domain.remote.entity.GenerationEvent
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

class ChatRemoteRepository @Inject constructor() {

    companion object {
        private const val TAG = "ChatRemoteRepository"
    }

    suspend fun getAIAnswer(model: Model, messageList: List<MessageEntity>): String =
        withContext(Dispatchers.IO) {
            return@withContext AIFactory.createAIModel(model).getAIAnswer(messageList)
        }

    suspend fun getAIStreamingAnswer(
        model: Model,
        messageList: List<MessageEntity>,
        callback: (GenerationEvent) -> Unit,
    ) = withContext(Dispatchers.IO) {
        AIFactory.createAIModel(model).getAIStreamingAnswer(messageList, callback)
    }

    fun streamingGen(model: Model, messageList: List<MessageEntity>): Flow<GenerationEvent> =
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
                        }
                        trySend(event)
                        close()
                    }
                }
            }
            try {
                Log.d(TAG, "[streamingGen] start to steaming...")
                getAIStreamingAnswer(model, messageList, callback)
            } catch (e: Exception) {
                trySend(GenerationEvent.Error("fail to streaming：${e.message}"))
                close()
            }
            awaitClose {
                Log.d(TAG, "[streamingGen] close...")
            }
        }.buffer(Channel.UNLIMITED).onEach { delay(5) }.flowOn(Dispatchers.IO)
}