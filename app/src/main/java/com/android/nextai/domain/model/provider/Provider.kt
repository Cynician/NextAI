package com.android.nextai.domain.model.provider

object ProviderType {
    const val QWEN = 0
    const val OPENAI = 1
    const val OTHER = 2
    const val CLAUDE = 3
}

data class Provider(
    val id: String,
    val name: String,
    val desc: String,
    val type: Int,
    val apiUrl: String,
    val apiKey: String,
    val models: List<Model> = emptyList(),
    val isOK: Boolean = false,
)

data class Providers(
    val defaultProviderId: String,
    val providers: List<Provider>,
)