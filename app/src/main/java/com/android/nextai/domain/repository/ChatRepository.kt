package com.android.nextai.domain.repository

import android.content.Context
import android.util.Log
import com.android.nextai.domain.remote.AIFactory
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepository @Inject constructor(
    @ApplicationContext context: Context,
) {

    companion object {
        private const val TAG = "ChatRepository"
    }

    suspend fun getAIAnswer(model: Model, messageList: List<Message>): String = withContext(Dispatchers.IO) {
            return@withContext AIFactory.createAIModel(model).getAIAnswer(messageList)
    }

    suspend fun getAIStreamingAnswer(model: Model, messageList: List<Message>, callback:(GenerationEvent)->Unit) = withContext(Dispatchers.IO) {
         AIFactory.createAIModel(model).getAIStreamingAnswer(messageList, callback)
    }

    fun streamingGen(model: Model, messageList: List<Message>): Flow<GenerationEvent> =
        callbackFlow<GenerationEvent> {
            val callback: (GenerationEvent) -> Unit = { event ->
                val result = trySend(event)
                if (result.isFailure) {
                    close() // close the flow
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
                Log.d(TAG, "[streamingGen] cancel...")
            }

        }.buffer(Channel.UNLIMITED)
            .flowOn(Dispatchers.IO)
}