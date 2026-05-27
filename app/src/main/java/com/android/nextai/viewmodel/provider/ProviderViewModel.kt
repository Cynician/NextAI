package com.android.nextai.viewmodel.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.datastore.entity.ProviderType
import com.android.nextai.domain.remote.utils.ModelManager
import com.android.nextai.domain.repository.ProviderRepository
import com.android.nextai.viewmodel.provider.entity.ProviderValidateEvent
import com.android.nextai.viewmodel.provider.entity.ProviderValidateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val repository: ProviderRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureProviderExists(
                type = ProviderType.QWEN
            )
        }
    }

    /**
     * Info
     */
    private val _curProvider = MutableStateFlow<ProviderEntity?>(null)

    val providers = repository.providersFlow
    val defaultProvider = repository.defaultProviderFlow
    val curProvider = _curProvider.asStateFlow()

    /**
     * State
     */
    private val _saveProviderState = MutableSharedFlow<Result<Unit>>()
    private val _providerValidateState = MutableStateFlow<ProviderValidateState>(ProviderValidateState.Idle)

    val saveProviderState = _saveProviderState.asSharedFlow()
    val providerValidateState = _providerValidateState.asStateFlow()

    /**
     * Event
     */
    private val _providerValidateEvent = MutableSharedFlow<ProviderValidateEvent>()

    val providerValidateEvent = _providerValidateEvent.asSharedFlow()


    fun addProvider(
        provider: ProviderEntity,
    ) {
        viewModelScope.launch {
            repository.addProvider(provider)
        }
    }

    fun setCurrentProvider(
        providerId: String,
    ) {
        viewModelScope.launch {
            _curProvider.value = repository.getProviderById(providerId)
        }
    }

    fun deleteProvider(
        providerId: String,
    ) {
        viewModelScope.launch {
            repository.deleteProvider(providerId)
        }
    }

    fun updateProvider(
        apiUrl: String,
        apiKey: String,
        model: String,
        isOK: Boolean,
    ) {
        viewModelScope.launch {
            runCatching {
                val updatedProvider = _curProvider.value?.copy(
                    apiUrl = apiUrl,
                    apiKey = apiKey,
                    model = model,
                    isOK = isOK
                )
                updatedProvider?.let {
                    _curProvider.value = it
                    repository.updateProvider(it)
                }
            }.onSuccess {
                _saveProviderState.emit(Result.success(Unit))
            }.onFailure {
                _saveProviderState.emit(Result.failure(it))
            }
        }
    }

    fun checkModelValidity(apiUrl: String, apiKey: String, model: String) {
        viewModelScope.launch {
            _providerValidateState.value = ProviderValidateState.Validating
            runCatching {
                ModelManager(
                    baseUrl = apiUrl,
                    apiKey = apiKey
                ).checkModelExists(model)
            }.onSuccess { result ->
                if (result.success) {
                    _providerValidateEvent.emit(ProviderValidateEvent.Success)
                } else {
                    _providerValidateEvent.emit(
                        ProviderValidateEvent.Error(result.message)
                    )
                }
            }.onFailure {
                _providerValidateEvent.emit(
                    ProviderValidateEvent.Error(it.message ?: "Unknown error")
                )
            }
            _providerValidateState.value = ProviderValidateState.Idle
        }
    }
}