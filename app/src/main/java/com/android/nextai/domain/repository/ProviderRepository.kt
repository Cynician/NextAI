package com.android.nextai.domain.repository

import com.android.nextai.domain.model.provider.Provider
import kotlinx.coroutines.flow.Flow

interface ProviderRepository {

    fun getDefaultProviderFlow(): Flow<Provider?>
    fun getAllProvidersFlow(): Flow<List<Provider>>
    suspend fun getProviderById(id: String): Provider?
    suspend fun saveProvider(provider: Provider)
    suspend fun updateProvider(provider: Provider)
    suspend fun deleteProviderById(id: String)
    suspend fun setDefaultProvider(id: String)
    suspend fun ensureProvidersExists(typeList: List<Int>)

}