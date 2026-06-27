package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class DeleteTailMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(msgId: Long, sessionId: Long): Result<Unit> = execute {
        chatRepository.deleteTailMessages(msgId, sessionId)
    }

}
