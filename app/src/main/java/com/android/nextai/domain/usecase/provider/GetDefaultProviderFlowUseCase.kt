package com.android.nextai.domain.usecase.provider

import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDefaultProviderFlowUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
) {

    operator fun invoke(): Flow<Provider?> = providerRepository.getDefaultProviderFlow()

}