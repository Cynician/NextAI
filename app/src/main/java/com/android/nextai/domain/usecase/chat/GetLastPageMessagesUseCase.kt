package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetLastPageMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(sessionId: Long, minMsgId: Long): Result<List<Message>> = execute {
        chatRepository.getLastPageMessages(sessionId, minMsgId).reversed()
    }

}