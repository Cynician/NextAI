package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class CreateMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(
        sessionId: Long,
        providerName: String = "",
        modelId: String = "",
        content: String,
        messageType: Int,
    ): Result<Message> = execute {
        chatRepository.createMessage(
            sessionId = sessionId,
            providerName = providerName,
            modelId = modelId,
            content = content,
            messageType = messageType,
        )
    }

}