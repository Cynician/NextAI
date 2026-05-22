package com.android.nextai.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.nextai.domain.database.datastore.PreferenceKeys
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.datastore.entity.ProvidersEntity
import com.android.nextai.domain.database.datastore.entity.ProviderType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    //Memory cache
    private var settingsCache = ProvidersEntity()
    private var initialized = false


    suspend fun initialize() {
        if (initialized) { return }
        val preferences = dataStore.data.first()
        val settingsJson = preferences[PreferenceKeys.PROVIDER_SETTINGS]
        settingsCache =
            if (settingsJson.isNullOrEmpty()) {
                ProvidersEntity()
            } else {
                json.decodeFromString(settingsJson)
            }
        initialized = true
    }

    /**
     * Get all settings
     */
    suspend fun getSettings(): ProvidersEntity {
        initialize()
        return settingsCache
    }

    /**
     * Get Providers
     */
    suspend fun getProviders(): List<ProviderEntity> {
        initialize()
        return settingsCache.providers
    }


    /**
     * Get Provider by id
     */
    suspend fun getProviderById(
        providerId: String,
    ): ProviderEntity? {
        initialize()
        return settingsCache.providers.find { it.id == providerId }
    }

    /**
     * Add Provider
     */
    suspend fun addProvider(
        provider: ProviderEntity,
    ) {
        initialize()
        settingsCache = settingsCache.copy(providers = settingsCache.providers + provider)
        persist()
    }

    /**
     * Update Provider
     */
    suspend fun updateProvider(
        provider: ProviderEntity,
    ) {
        initialize()
        settingsCache = settingsCache.copy(
            providers = settingsCache.providers.map {
                if (it.id == provider.id) {
                    provider
                } else {
                    it
                }
            }
        )
        persist()
    }

    /**
     * Delete Provider
     */
    suspend fun deleteProvider(
        providerId: String,
    ) {
        initialize()
        val updatedProviders = settingsCache.providers.filterNot { it.id == providerId }
        settingsCache = settingsCache.copy(providers = updatedProviders)
        persist()
    }

    /**
     * Ensure the Provider exists
     */
    suspend fun ensureProviderExists(
        type: ProviderType
    ) {
        initialize()
        val exists = settingsCache.providers.any { it.type == type }
        if (exists) { return }

        val provider = when (type) {

            ProviderType.QWEN -> {
                ProviderEntity(
                    name = "通义千问",
                    type = type,
                    model = "qwen-max",
                    apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
                    desc = "阿里巴巴通义大模型系列"
                )
            }

            ProviderType.OPENAI -> {
                ProviderEntity(
                    name = "OpenAI",
                    type = type,
                    model = "gpt-4o",
                    apiUrl = "https://api.openai.com/v1"
                )
            }

            ProviderType.CLAUDE -> {
                ProviderEntity(
                    name = "Claude",
                    type = type,
                    model = "claude-sonnet-4-0",
                    apiUrl = "https://api.anthropic.com"
                )
            }

            else -> {
                ProviderEntity(
                    name = type.name,
                    type = type
                )
            }
        }

        addProvider(provider)
    }

    /**
     * Persistence
     */
    private suspend fun persist() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.PROVIDER_SETTINGS] = json.encodeToString(settingsCache)
        }
    }
}