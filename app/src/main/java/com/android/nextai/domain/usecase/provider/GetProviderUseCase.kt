package com.android.nextai.domain.usecase.provider

import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.repository.ProviderRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetProviderUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
) : BaseUseCase() {

    suspend operator fun invoke(id: String): Result<Provider?> = execute {
        providerRepository.getProviderById(id)
    }

}