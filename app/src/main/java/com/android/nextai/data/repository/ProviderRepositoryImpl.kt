package com.android.nextai.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.nextai.data.datasource.datebase.datastore.PreferenceKeys
import com.android.nextai.data.datasource.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datasource.datebase.datastore.entity.ProvidersEntity
import com.android.nextai.data.datasource.datebase.datastore.entity.toDomain
import com.android.nextai.data.datasource.datebase.datastore.entity.toEntity
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.provider.ProviderType
import com.android.nextai.domain.model.provider.Providers
import com.android.nextai.domain.repository.ProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ProviderRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ProviderRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }


    private suspend fun getRawSettings(): ProvidersEntity {
        val settingsJson = dataStore.data.map { it[PreferenceKeys.PROVIDER] }.first()
        return if (settingsJson.isNullOrEmpty()) {
            ProvidersEntity()
        } else {
            runCatching { json.decodeFromString<ProvidersEntity>(settingsJson) }.getOrElse { ProvidersEntity() }
        }
    }

    /**
     * Single Source Of Truth - Trans to Flow<Providers>
     */
    val settingsFlow: Flow<Providers> =
        dataStore.data.map { preferences ->
            val settingsJson = preferences[PreferenceKeys.PROVIDER]
            val entity = if (settingsJson.isNullOrEmpty()) {
                ProvidersEntity()
            } else {
                runCatching { json.decodeFromString<ProvidersEntity>(settingsJson) }.getOrElse { ProvidersEntity() }
            }
            entity.toDomain()
        }

    private suspend fun getSettings(): Providers = settingsFlow.first()

    /**
     * Observe providers
     */
    private val providersFlow: Flow<List<Provider>> = settingsFlow.map { it.providers }

    /**
     * Observe default provider
     */
    private val defaultProviderFlow: Flow<Provider?> =
        settingsFlow.map { settings ->
            settings.providers.find { it.id == settings.defaultProviderId }
                ?: settings.providers.firstOrNull { it.isOK }
        }

    override fun getDefaultProviderFlow(): Flow<Provider?> = defaultProviderFlow.catch { e ->
        // log error
        emit(null)
    }


    override fun getAllProvidersFlow(): Flow<List<Provider>> = providersFlow.catch { e ->
        emit(emptyList())
    }

    /**
     * Update helper
     */
    private suspend fun updateSettings(
        transform: (ProvidersEntity) -> ProvidersEntity,
    ) {
        dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.PROVIDER]?.let {
                runCatching { json.decodeFromString<ProvidersEntity>(it) }.getOrNull()
            } ?: ProvidersEntity()

            val updated = transform(current)
            preferences[PreferenceKeys.PROVIDER] = json.encodeToString(updated)
        }
    }

    /**
     * Get providers
     */
    private suspend fun getAllProviders(): List<Provider> = getSettings().providers

    /**
     * Get provider by id
     */
    override suspend fun getProviderById(id: String): Provider? {
        return getAllProviders().find { it.id == id }
    }


    /**
     * Add provider
     */
    override suspend fun saveProvider(provider: Provider) {
        updateSettings { settings ->
            settings.copy(providers = settings.providers + provider.toEntity())
        }
    }

    /**
     * Update provider
     */
    override suspend fun updateProvider(provider: Provider) {
        updateSettings { settings ->
            val updatedProviders = settings.providers.map {
                if (it.id == provider.id) provider.toEntity() else it
            }
            settings.copy(providers = updatedProviders)
        }
    }

    /**
     * Delete provider
     */
    override suspend fun deleteProviderById(id: String) {
        updateSettings { settings ->
            val updatedProviders = settings.providers.filterNot { it.id == id }
            val updatedDefaultProviderId = if (settings.defaultProviderId == id) {
                updatedProviders.firstOrNull { it.isOK }?.id
            } else {
                settings.defaultProviderId
            }

            settings.copy(
                providers = updatedProviders,
                defaultProviderId = updatedDefaultProviderId
            )
        }
    }

    /**
     * Set default provider
     */
    override suspend fun setDefaultProvider(id: String) {
        updateSettings { settings ->
            if (settings.providers.none { it.id == id }) {
                settings
            } else {
                settings.copy(defaultProviderId = id)
            }
        }
    }

    /**
     * Ensure provider exists
     */
    override suspend fun ensureProvidersExists(typeList: List<Int>) {
        val rawSettings = getRawSettings()
        val existingTypes = rawSettings.providers.map { it.type }.toSet()

        val missingTypes = typeList
            .filter { it !in existingTypes }
            .distinct()

        if (missingTypes.isEmpty()) return

        val newProviders = missingTypes.map { type ->
            when (type) {
                ProviderType.QWEN -> ProviderEntity(
                    name = "通义千问",
                    type = ProviderType.QWEN,
                    apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
                    desc = "阿里巴巴通义大模型系列"
                )

                ProviderType.OPENAI -> ProviderEntity(
                    name = "OpenAI",
                    type = ProviderType.OPENAI,
                    apiUrl = "https://api.openai.com/v1",
                    desc = "OpenAI 大模系列"
                )

                ProviderType.CLAUDE -> ProviderEntity(
                    name = "Claude",
                    type = ProviderType.CLAUDE,
                    apiUrl = "https://api.anthropic.com",
                    desc = "Claude 大模型系列"
                )

                else -> ProviderEntity(
                    name = "自定义",
                    type = ProviderType.OTHER,
                    desc = "自定义大模型系列"
                )
            }
        }

        updateSettings { settings ->
            settings.copy(
                providers = settings.providers + newProviders
            )
        }
    }


}