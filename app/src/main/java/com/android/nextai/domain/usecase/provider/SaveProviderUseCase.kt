package com.android.nextai.domain.usecase.provider

import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.repository.ProviderRepository
import com.android.nextai.domain.usecase.BaseUseCase
import javax.inject.Inject

class SaveProviderUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
) : BaseUseCase() {

    suspend operator fun invoke(provider: Provider): Result<Unit> = execute {
        providerRepository.saveProvider(provider)
    }

}