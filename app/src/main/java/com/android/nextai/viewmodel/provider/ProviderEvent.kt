package com.android.nextai.viewmodel.provider

import com.android.nextai.data.datebase.datastore.entity.ModelEntity


sealed interface ProviderEvent {
    data class RetrieveModels(
        val success: Boolean,
        val data: List<ModelEntity> = emptyList(),
        val message: String = "",
    ) : ProviderEvent
}