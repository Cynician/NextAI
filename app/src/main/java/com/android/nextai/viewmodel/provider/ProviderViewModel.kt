package com.android.nextai.viewmodel.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.data.datebase.datastore.entity.ModelEntity
import com.android.nextai.data.datebase.datastore.entity.ProviderEntity
import com.android.nextai.data.datebase.datastore.entity.ProviderType
import com.android.nextai.domain.model.ApiResult
import com.android.nextai.data.remote.utils.ModelManager
import com.android.nextai.repository.ProviderRepository
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

    /** Retrieve models Job(Single). **/
    private var retrieveModelsJob: Job? = null

    /**
     * The currently selected model provider will be set when setting or adding a model provider
     * (the initial value is read from the database), if in addition mode, this value is null.
     */
    private val _curProvider = MutableStateFlow<ProviderEntity?>(null)
    val curProvider = _curProvider.asStateFlow()

    /** All model providers and databases are delivered directly via "Flow", avoiding complicated CRUDs. **/
    val providers = repository.providersFlow

    /** The default model provider is currently set to the first provider to be successfully configured. **/
    val defaultProvider = repository.defaultProviderFlow


    /** The status of the remote request retrieval model, including idle, in retrieval。 **/
    private val _retrieveModelsState = MutableStateFlow<ProviderState>(ProviderState.Idle)
    val retrieveModelsState = _retrieveModelsState.asStateFlow()

    /**
     * Various configurable information for the model provider, recording data interacting with
     * the input box.
     * The initial value is the current model provider's information, and the addition mode is set
     * to default value.
     */
    private val _providerSettingState = MutableStateFlow(ProviderSettingState())
    val providerSettingState = _providerSettingState.asStateFlow()

    /**
     * Records the list of available models from the current model provider as well as the list of
     * selected models.
     *
     * - The list of available models is obtained via remote request.
     *
     * - The initial value of the selected model list is provided by the current model provider. In
     * the selected mode, it is empty. Through interaction, user can add models from the list of
     * selected models to the selected model list.
     * */
    private val _providerModelsState = MutableStateFlow(ProviderModelsState())
    val providerModelsState = _providerModelsState.asStateFlow()

    /**
     * Records whether the current model provider information has changed, mainly by comparing it
     * with the data of "_providerSettingState" and "_providerModelsState".
     */
    val isProviderSettingChanged: StateFlow<Boolean> = combine(
        _providerSettingState, _providerModelsState, _curProvider
    ) { settingState, modelsState, provider ->
        if (provider == null) {
            return@combine (settingState.name.isNotBlank() || settingState.desc.isNotBlank() || settingState.apiUrl.isNotBlank() || settingState.apiKey.isNotBlank() || modelsState.selectedModels.isNotEmpty())
        }
        settingState.name != provider.name || settingState.desc != provider.desc || settingState.apiUrl != provider.apiUrl || settingState.apiKey != provider.apiKey || modelsState.selectedModels != provider.models
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false
    )

    /** Regarding events involving model providers, the main focus is on recording the results after the event is completed. **/
    private val _providerEvent = MutableSharedFlow<ProviderEvent>()

    /**
     * When adding model provider, initialize some variable information.
     */
    fun addProviderInit() {
        _curProvider.value = null
        _providerSettingState.value = ProviderSettingState()
        _providerModelsState.value = ProviderModelsState()
    }

    /**
     * Select the current model provider, and the entry point is the model providers screen.
     */
    fun selectCurrentProvider(
        providerId: String,
    ) {
        viewModelScope.launch {
            _curProvider.value = repository.getProviderById(providerId)
            _curProvider.value?.let {
                _providerSettingState.value = ProviderSettingState(
                    name = it.name,
                    desc = it.desc,
                    apiUrl = it.apiUrl,
                    apiKey = it.apiKey,
                    isOK = it.isOK,
                )
                _providerModelsState.value = ProviderModelsState(
                    selectedModels = it.models
                )
                retrieveModels(it.apiUrl, it.apiKey, true)
            }
        }
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
        isFirstTime: Boolean = false,
    ) {
        retrieveModelsJob?.cancel()
        retrieveModelsJob = viewModelScope.launch {
            _retrieveModelsState.value = ProviderState.RetrievingModels
            ModelManager(
                baseUrl = apiUrl, apiKey = apiKey
            ).retrievingModels().let { result ->
                when (result) {

                    is ApiResult.Success -> {
                        val modelList = result.data ?: emptyList()
                        val isOK = modelList.isNotEmpty()
                        onRetrieveModelsSuccess(modelList, isOK, isFirstTime)
                    }

                    is ApiResult.Error -> {
                        onRetrieveModelsFailure(isFirstTime, result.message)
                    }

                }
            }
            _retrieveModelsState.value = ProviderState.Idle
        }
    }

    private suspend fun onRetrieveModelsSuccess(
            modelList: List<ModelEntity>,
            isOK: Boolean,
            isFirstTime: Boolean,
        ){
        _providerModelsState.update { it.copy(availableModels = modelList) }
        _providerSettingState.update { it.copy(isOK = isOK) }
        // Not add provider mode and not the first time auto retrieve models.
        _curProvider.value?.let {
            if(!isFirstTime) return@let
            val saveP = it.copy(
                isOK = isOK && it.models.isNotEmpty(),
                models = if (!isOK) emptyList() else it.models
            )
            saveProviderSetting(saveP)
            _curProvider.value = saveP
        }

        _providerEvent.emit(ProviderEvent.RetrieveModels(success = true, data = modelList))
    }

    private suspend fun onRetrieveModelsFailure(
        isFirstTime: Boolean,
        errorMsg: String,
    ){
        _providerSettingState.update { it.copy(isOK = false) }
        _providerModelsState.value = ProviderModelsState()
        // Not add provider mode and not the first time auto retrieve models.
        _curProvider.value?.let {
            if(!isFirstTime) return@let
            val saveP = it.copy(isOK = false, models = emptyList())
            saveProviderSetting(saveP)
            _curProvider.value = saveP
        }
        _providerEvent.emit(ProviderEvent.RetrieveModels(success = true, message = errorMsg))
    }

    /** Save all changed info. **/
    fun saveProviderSetting() {
        viewModelScope.launch {
            val provider = ProviderEntity(
                id = _curProvider.value?.id ?: UUID.randomUUID().toString(),
                type = _curProvider.value?.type ?: ProviderType.OTHER,
                name = _providerSettingState.value.name.trim(),
                desc = _providerSettingState.value.desc.trim(),
                apiUrl = _providerSettingState.value.apiUrl.trim(),
                apiKey = _providerSettingState.value.apiKey.trim(),
                models = _providerModelsState.value.selectedModels,
                isOK = _providerSettingState.value.isOK
            )
            if (_curProvider.value == null) {
                repository.addProvider(provider)
            } else {
                repository.updateProvider(provider)
            }
            _curProvider.value = provider
        }
    }

    /** Save partial changed information **/
    fun saveProviderSetting(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.updateProvider(provider)
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
                    isOK = _curProvider.value?.isOK ?: false,
                )
            }
            _providerModelsState.update {
                it.copy(selectedModels = _curProvider.value?.models ?: emptyList())
            }
        }
    }

    /** Provider name changed. **/
    fun onProviderNameChanged(newName: String) {
        _providerSettingState.update { it.copy(name = newName) }
    }

    /** Provider description changed. **/
    fun onProviderDescChanged(newDesc: String) {
        _providerSettingState.update { it.copy(desc = newDesc) }
    }

    /** Api Url changed then init states. **/
    fun onApiUrlChanged(newUrl: String) {
        _providerSettingState.update {
            it.copy(
                apiUrl = newUrl,
                isOK = newUrl == _curProvider.value?.apiUrl && _curProvider.value?.isOK == true
            )
        }
        _providerModelsState.update { it.copy(availableModels = emptyList()) }
    }

    /** Api key changed then then init states. **/
    fun onApiKeyChanged(newKey: String) {
        _providerSettingState.update {
            it.copy(
                apiKey = newKey,
                isOK = newKey == _curProvider.value?.apiKey && _curProvider.value?.isOK == true
            )
        }
        _providerModelsState.update { it.copy(availableModels = emptyList()) }
    }

    /** Add a model to the selected models list. **/
    fun addSelectedModel(model: ModelEntity) {
        _providerModelsState.update { state ->
            if (state.selectedModels.contains(model)) {
                state
            } else {
                state.copy(selectedModels = state.selectedModels + model)
            }
        }
    }

    /** Remove a model from the selected models list. **/
    fun removeSelectedModel(model: ModelEntity) {
        _providerModelsState.update { state ->
            state.copy(selectedModels = state.selectedModels - model)
        }
    }
}