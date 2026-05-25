package com.android.nextai.viewmodel.provider.entity

sealed interface ProviderValidateState {

    data object Idle : ProviderValidateState

    data object Validating : ProviderValidateState
}

sealed interface ProviderValidateEvent {

    data object Success : ProviderValidateEvent

    data class Error(
        val message: String
    ) : ProviderValidateEvent
}