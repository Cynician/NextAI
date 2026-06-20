package com.android.nextai.data.datasource.remote

import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent

interface AIDataSource {

    suspend fun getAIStreamingAnswer(
        messageList: List<Message>,
        provider: Provider,
        modelId: String,
        generationCallback: (GenerationEvent) -> Unit,
    )
}