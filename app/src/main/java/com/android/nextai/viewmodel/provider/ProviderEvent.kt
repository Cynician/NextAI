package com.android.nextai.viewmodel.provider

import com.android.nextai.domain.model.provider.Model


sealed interface ProviderEvent {
    data class RetrieveModels(
        val success: Boolean,
        val data: List<Model> = emptyList(),
        val message: String = "",
    ) : ProviderEvent
}