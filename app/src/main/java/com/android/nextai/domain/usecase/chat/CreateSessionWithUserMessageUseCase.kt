package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.Session
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class CreateSessionWithUserMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(
        query: String,
        provider: Provider,
        modelId: String,
    ): Result<Pair<Session, Message>> = execute {
        chatRepository.createSessionWithUserMessage(
            query = query,
            modelParamsId = "0", // New session with default model params
            provider = provider,
            modelId = modelId,
        )
    }

}