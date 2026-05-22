package com.android.nextai.domain.database.datastore.entity

import kotlinx.serialization.Serializable
import java.util.UUID


enum class ProviderType {
    QWEN,
    OPENAI,
    CUSTOM,
    CLAUDE,
}

@Serializable
data class ProviderEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ProviderType,
    val apiUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val desc: String = "",
    val isOK: Boolean = false
)

@Serializable
data class ProvidersEntity(
    val providers: List<ProviderEntity> = emptyList()
)