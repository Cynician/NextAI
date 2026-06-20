package com.android.nextai.data.datasource.datebase.datastore.entity

import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.provider.ProviderType
import com.android.nextai.domain.model.provider.Providers
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
data class ProviderEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val desc: String = "",
    val type: Int = ProviderType.OTHER,
    val apiUrl: String = "",
    val apiKey: String = "",
    val models: List<ModelEntity> = emptyList(),
    val isOK: Boolean = false,
)

@Serializable
data class ProvidersEntity(
    val defaultProviderId: String? = null,
    val providers: List<ProviderEntity> = emptyList(),
)

fun ProviderEntity.toDomain(): Provider = Provider(
    id = this.id,
    name = this.name,
    desc = this.desc,
    type = this.type,
    apiUrl = this.apiUrl,
    apiKey = this.apiKey,
    models = this.models.map { it.toDomain() },
    isOK = this.isOK
)

fun Provider.toEntity(): ProviderEntity = ProviderEntity(
    id = this.id,
    name = this.name,
    desc = this.desc,
    type = this.type,
    apiUrl = this.apiUrl,
    apiKey = this.apiKey,
    models = this.models.map { it.toEntity() },
    isOK = this.isOK
)

fun ProvidersEntity.toDomain(): Providers = Providers(
    defaultProviderId = this.defaultProviderId ?: "",
    providers = this.providers.map { it.toDomain() })