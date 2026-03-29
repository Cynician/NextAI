package com.android.nextai.domain.remote

import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.viewmodel.chat.entity.Message

interface AIModelDataSource {
    suspend fun getAIAnswer(messageList:List<Message>): String

    suspend fun getAIStreamingAnswer(messageList:List<Message>, callback:(GenerationEvent)->Unit)
}