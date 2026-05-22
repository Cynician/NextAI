package com.android.nextai.domain.remote

import com.android.nextai.domain.database.sqlite.entity.MessageEntity
import com.android.nextai.domain.remote.entity.GenerationEvent

interface AIModelDataSource {
    suspend fun getAIAnswer(messageList:List<MessageEntity>): String

    suspend fun getAIStreamingAnswer(messageList:List<MessageEntity>, callback:(GenerationEvent)->Unit)
}