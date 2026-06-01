package com.android.nextai.viewmodel.provider.entity

import com.android.nextai.domain.database.datastore.entity.ModelEntity


sealed interface ProviderState {
    data object RetrievingModels : ProviderState
    data object Idle : ProviderState
}

data class ProviderSettingState(
    var name: String = "",
    var desc: String = "",
    var apiUrl: String = "",
    var apiKey: String = "",
)

data class ProviderModelsState(
    var availableModels: List<ModelEntity> = emptyList(),
    val selectedModels: List<ModelEntity> = emptyList(),
)