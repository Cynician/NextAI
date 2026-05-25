package com.android.nextai.viewmodel.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.datastore.entity.ProviderType
import com.android.nextai.domain.repository.ProviderRepository
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
            _providers.value = repository.getProviders()
        }
    }

    /**
     * Info
     */
    private val _providers = MutableStateFlow<List<ProviderEntity>>(emptyList())
    private val _curProvider = MutableStateFlow<ProviderEntity?>(null)

    val providers = _providers.asStateFlow()
    val curProvider = _curProvider.asStateFlow()

    /**
     * State
     */
    private val _saveState = MutableSharedFlow<Result<Unit>>()

    val saveState = _saveState.asSharedFlow()


    fun addProvider(
        provider: ProviderEntity,
    ) {
        viewModelScope.launch {
            repository.addProvider(provider)
            _providers.value = repository.getProviders()
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
            _providers.value = repository.getProviders()
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
                    _providers.value = repository.getProviders()
                }
            }.onSuccess {
                _saveState.emit(Result.success(Unit))
            }.onFailure {
                _saveState.emit(Result.failure(it))
            }
        }
    }
}