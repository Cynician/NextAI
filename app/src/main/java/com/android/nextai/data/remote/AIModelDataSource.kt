package com.android.nextai.data.remote

import com.android.nextai.domain.model.GenerationEvent
import com.android.nextai.data.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datebase.room.entity.MessageEntity

interface AIModelDataSource {
    suspend fun getAIAnswer(messageList:List<MessageEntity>): String

    suspend fun getAIStreamingAnswer(
        messageList: List<MessageEntity>,
        provider: ProviderEntity,
        callback: (GenerationEvent) -> Unit
    )
}