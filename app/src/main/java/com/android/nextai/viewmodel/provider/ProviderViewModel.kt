package com.android.nextai.viewmodel.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.database.datastore.entity.ProviderEntity
import com.android.nextai.domain.database.datastore.entity.ProviderType
import com.android.nextai.domain.remote.entity.ApiResult
import com.android.nextai.domain.remote.utils.ModelManager
import com.android.nextai.domain.repository.ProviderRepository
import com.android.nextai.viewmodel.provider.entity.ProviderEvent
import com.android.nextai.viewmodel.provider.entity.ProviderModelsState
import com.android.nextai.viewmodel.provider.entity.ProviderSettingState
import com.android.nextai.viewmodel.provider.entity.ProviderState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
     * Job
     */
    private var retrieveModelsJob: Job? = null

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
    private val _retrieveModelsState = MutableStateFlow<ProviderState>(ProviderState.Idle)
    //The items when setting the provider
    private val _providerSettingState = MutableStateFlow(ProviderSettingState())
    //List of available and selected models
    private val _providerModelsState = MutableStateFlow(ProviderModelsState())

    val retrieveModelsState = _retrieveModelsState.asStateFlow()
    val providerSettingState = _providerSettingState.asStateFlow()
    val providerModelsState = _providerModelsState.asStateFlow()

    val isProviderSettingChanged: StateFlow<Boolean> =
        combine(
            _providerSettingState,
            _providerModelsState,
            _curProvider
        ) { settingState, modelsState, provider ->

            if (provider == null) {
                return@combine (
                        settingState.name.isNotBlank() ||
                                settingState.desc.isNotBlank() ||
                                settingState.apiUrl.isNotBlank() ||
                                settingState.apiKey.isNotBlank() ||
                                modelsState.selectedModels.isNotEmpty()
                        )
            }

            settingState.name != provider.name ||
                    settingState.desc != provider.desc ||
                    settingState.apiUrl != provider.apiUrl ||
                    settingState.apiKey != provider.apiKey ||
                    modelsState.selectedModels != provider.models

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Event
     */
    private val _providerEvent = MutableSharedFlow<ProviderEvent>()


    fun setCurrentProvider(
        providerId: String,
    ) {
        viewModelScope.launch {
            _curProvider.value = repository.getProviderById(providerId)
            /** _providerSettingState init **/
            _curProvider.value?.let {
                _providerSettingState.value = ProviderSettingState(
                    name = it.name,
                    desc = it.desc,
                    apiUrl = it.apiUrl,
                    apiKey = it.apiKey
                )
                _providerModelsState.value = ProviderModelsState(
                    selectedModels = it.models
                )
            }
        }
    }

    fun initCurrentProvider() {
        _curProvider.value = null
        _providerSettingState.value = ProviderSettingState()
        _providerModelsState.value = ProviderModelsState()
    }

    fun deleteProvider(
        providerId: String,
    ) {
        viewModelScope.launch {
            repository.deleteProvider(providerId)
        }
    }


    fun retrieveModels(
        apiUrl: String,
        apiKey: String,
    ) {
        retrieveModelsJob?.cancel()
        retrieveModelsJob = viewModelScope.launch {
            _retrieveModelsState.value = ProviderState.RetrievingModels
            ModelManager(
                baseUrl = apiUrl,
                apiKey = apiKey
            ).retrievingModels().let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _providerEvent.emit(
                            ProviderEvent.RetrieveModels(
                                success = true,
                                data = result.data ?: emptyList()
                            )
                        )
                        _providerModelsState.update {
                            it.copy(
                                availableModels = result.data ?: emptyList()
                            )
                        }
                    }

                    is ApiResult.Error -> {
                        _providerEvent.emit(
                            ProviderEvent.RetrieveModels(
                                success = true,
                                message = result.message
                            )
                        )
                    }

                }

            }
            _retrieveModelsState.value = ProviderState.Idle
        }
    }

    fun updateProviderSettingState(
        newState: ProviderSettingState,
    ) {
        _providerSettingState.value = newState
    }

    fun updateProviderModelsState(
        newState: ProviderModelsState,
    ) {
        _providerModelsState.value = newState
    }

    fun saveProviderSetting() {
        viewModelScope.launch {
            val provider = ProviderEntity(
                id = _curProvider.value?.id ?: UUID.randomUUID().toString(),
                type = _curProvider.value?.type ?: ProviderType.OTHER,
                name = _providerSettingState.value.name,
                desc = _providerSettingState.value.desc,
                apiUrl = _providerSettingState.value.apiUrl,
                apiKey = _providerSettingState.value.apiKey,
                models = _providerModelsState.value.selectedModels,
                isOK = _providerModelsState.value.selectedModels.isNotEmpty()
            )
            if (_curProvider.value == null) {
                repository.addProvider(provider)
            } else {
                repository.updateProvider(provider)
            }

            _curProvider.value = provider
        }
    }

    fun resetProviderSetting() {
        viewModelScope.launch {
            _providerSettingState.update {
                it.copy(
                    name = _curProvider.value?.name ?: "",
                    desc = _curProvider.value?.desc ?: "",
                    apiUrl = _curProvider.value?.apiUrl ?: "",
                    apiKey = _curProvider.value?.apiKey ?: "",
                )
            }
            _providerModelsState.update {
                it.copy(selectedModels = _curProvider.value?.models ?: emptyList())
            }
        }
    }
}