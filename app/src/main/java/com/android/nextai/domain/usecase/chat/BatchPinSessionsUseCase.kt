package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class BatchPinSessionsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(ids: List<Long>): Result<Unit> = execute {
        chatRepository.pinSessionsByIds(ids)
    }

}