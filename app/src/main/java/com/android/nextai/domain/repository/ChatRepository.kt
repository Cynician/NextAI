package com.android.nextai.domain.repository

import android.content.Context
import com.android.nextai.domain.remote.AIFactory
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
}