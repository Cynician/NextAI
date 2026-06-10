package com.android.nextai.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.nextai.data.datebase.datastore.PreferenceKeys
import com.android.nextai.data.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datebase.datastore.entity.ProviderType
import com.android.nextai.data.datebase.datastore.entity.ProvidersEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    /**
     * Single Source Of Truth
     */
    val settingsFlow: Flow<ProvidersEntity> =
        dataStore.data.map { preferences ->

            val settingsJson =
                preferences[PreferenceKeys.PROVIDER_SETTINGS]

            if (settingsJson.isNullOrEmpty()) {
                ProvidersEntity()
            } else {
                runCatching {
                    json.decodeFromString<ProvidersEntity>(settingsJson)
                }.getOrElse {
                    ProvidersEntity()
                }
            }
        }

    /**
     * Get settings
     */
    suspend fun getSettings(): ProvidersEntity {
        return settingsFlow.first()
    }


    /**
     * Observe providers
     */
    val providersFlow = settingsFlow.map { it.providers }

    /**
     * Observe default provider
     */
    val defaultProviderFlow =
        settingsFlow.map { settings ->

            settings.providers.find {
                it.id == settings.defaultProviderId
            } ?: settings.providers.firstOrNull {
                it.isOK
            }
        }

    /**
     * Update helper
     */
    private suspend fun updateSettings(
        transform: (ProvidersEntity) -> ProvidersEntity
    ) {

        dataStore.edit { preferences ->

            val current =
                preferences[PreferenceKeys.PROVIDER_SETTINGS]
                    ?.let {
                        runCatching {
                            json.decodeFromString<ProvidersEntity>(it)
                        }.getOrNull()
                    }
                    ?: ProvidersEntity()

            val updated = transform(current)

            preferences[PreferenceKeys.PROVIDER_SETTINGS] =
                json.encodeToString(updated)
        }
    }

    /**
     * Get providers
     */
    suspend fun getProviders(): List<ProviderEntity> {
        return getSettings().providers
    }

    /**
     * Get provider by id
     */
    suspend fun getProviderById(
        providerId: String,
    ): ProviderEntity? {
        return getSettings()
            .providers
            .find { it.id == providerId }
    }

    /**
     * Add provider
     */
    suspend fun addProvider(
        provider: ProviderEntity,
    ) {

        updateSettings { settings ->

            settings.copy(
                providers = settings.providers + provider
            )
        }
    }

    /**
     * Update provider
     */
    suspend fun updateProvider(
        provider: ProviderEntity,
    ) {
        updateSettings { settings ->
            val updatedProviders =
                settings.providers.map {
                    if (it.id == provider.id) provider
                    else it
                }
            settings.copy(providers = updatedProviders)
        }
    }

    /**
     * Delete provider
     */
    suspend fun deleteProvider(
        providerId: String,
    ) {

        updateSettings { settings ->

            val updatedProviders =
                settings.providers.filterNot {
                    it.id == providerId
                }

            val updatedDefaultProviderId =
                if (settings.defaultProviderId == providerId) {
                    updatedProviders
                        .firstOrNull { it.isOK }
                        ?.id
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
    suspend fun setDefaultProvider(
        providerId: String,
    ) {
        updateSettings { settings ->
            val exists =
                settings.providers.any {
                    it.id == providerId
                }

            if (!exists) {
                settings
            } else {
                settings.copy(
                    defaultProviderId = providerId
                )
            }
        }
    }

    /**
     * Ensure provider exists
     */
    suspend fun ensureProviderExists(
        type: ProviderType
    ) {

        val exists = getProviders().any { it.type == type }

        if (exists) return

        val provider = when (type) {

            ProviderType.QWEN -> {
                ProviderEntity(
                    name = "通义千问",
                    type = type,
                    models = emptyList(),
                    apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
                    desc = "阿里巴巴通义大模型系列"
                )
            }

            ProviderType.OPENAI -> {
                ProviderEntity(
                    name = "OpenAI",
                    type = type,
                    models = emptyList(),
                    apiUrl = "https://api.openai.com/v1"
                )
            }

            ProviderType.CLAUDE -> {
                ProviderEntity(
                    name = "Claude",
                    type = type,
                    models = emptyList(),
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
}