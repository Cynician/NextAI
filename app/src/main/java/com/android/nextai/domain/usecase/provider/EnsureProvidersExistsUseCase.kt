package com.android.nextai.domain.usecase.provider

import com.android.nextai.domain.model.provider.ProviderType
import com.android.nextai.domain.repository.ProviderRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class EnsureProvidersExistsUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
) : BaseUseCase() {

    suspend operator fun invoke(): Result<Unit> = execute {
        val typeList = listOf(
            ProviderType.QWEN, ProviderType.OPENAI, ProviderType.CLAUDE, ProviderType.OTHER
        )
        providerRepository.ensureProvidersExists(typeList)
    }

}