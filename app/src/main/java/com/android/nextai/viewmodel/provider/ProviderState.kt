package com.android.nextai.viewmodel.provider

import com.android.nextai.domain.model.provider.Model


sealed interface ProviderState {
    data object RetrievingModels : ProviderState
    data object Idle : ProviderState
}

data class ProviderSettingState(
    var name: String = "",
    var desc: String = "",
    var apiUrl: String = "",
    var apiKey: String = "",
    var isOK: Boolean = false,
)

data class ProviderModelsState(
    var availableModels: List<Model> = emptyList(),
    val selectedModels: List<Model> = emptyList(),
)