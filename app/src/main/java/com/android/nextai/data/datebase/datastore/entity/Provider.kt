package com.android.nextai.data.datebase.datastore.entity

import kotlinx.serialization.Serializable
import java.util.UUID


enum class ProviderType {
    QWEN,
    OPENAI,
    OTHER,
    CLAUDE,
}

@Serializable
data class ProviderEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val desc: String = "",
    val type: ProviderType = ProviderType.OTHER,
    val apiUrl: String = "",
    val apiKey: String = "",
    val models: List<ModelEntity> = emptyList(),
    val isOK: Boolean = false
)

@Serializable
data class ProvidersEntity(
    val defaultProviderId: String? = null,
    val providers: List<ProviderEntity> = emptyList()
)