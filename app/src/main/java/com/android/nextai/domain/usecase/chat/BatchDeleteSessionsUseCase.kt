package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class BatchDeleteSessionsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(isSoftDelete: Boolean, ids: List<Long>): Result<Unit> = execute {
        chatRepository.deleteSessionsByIds(isSoftDelete, ids)
    }

}