package com.android.nextai.viewmodel.provider.entity

import com.android.nextai.domain.database.datastore.entity.ModelEntity


sealed interface ProviderEvent {
    data class RetrieveModels(
        val success: Boolean,
        val data: List<ModelEntity> = emptyList(),
        val message: String = "",
    ) : ProviderEvent
}