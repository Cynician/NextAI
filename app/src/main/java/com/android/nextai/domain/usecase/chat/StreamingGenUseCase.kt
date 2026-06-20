package com.android.nextai.domain.usecase.chat

import com.android.nextai.data.datasource.remote.ApiType
import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class StreamingGenUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(
        apiType: ApiType,
        messageList: List<Message>,
        provider: Provider,
        modelId: String,
    ): Result<Flow<GenerationEvent>> = execute {
        chatRepository.streamingGen(
            apiType = apiType,
            messageList = messageList,
            provider = provider,
            modelId = modelId,
        )
    }

}