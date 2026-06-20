package com.android.nextai.domain.usecase.provider

import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProvidersFlowUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
) {

    operator fun invoke(): Flow<List<Provider>> = providerRepository.getAllProvidersFlow()

}