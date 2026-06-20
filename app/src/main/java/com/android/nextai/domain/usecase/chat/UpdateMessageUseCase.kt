package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject


class UpdateMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(
        id: Long,
        sessionId: Long,
        providerName: String? = null,
        modelId: String? = null,
        reasoningContent: String? = null,
        content: String? = null,
        status: Int? = null,
        tokenCount: Int? = null,
        extra: String? = null,
        updatedAt: Long? = null,
    ): Result<Unit> = execute {
        chatRepository.updateMessage(
            id,
            sessionId = sessionId,
            providerName = providerName,
            modelId = modelId,
            reasoningContent = reasoningContent,
            content = content,
            status = status,
            tokenCount = tokenCount,
            extra = extra,
            updatedAt = updatedAt
        )
    }

}