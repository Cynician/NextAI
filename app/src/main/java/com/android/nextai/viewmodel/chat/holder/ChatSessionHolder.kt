package com.android.nextai.viewmodel.chat.holder

import com.android.nextai.domain.database.db.entity.SessionEntity
import com.android.nextai.domain.repository.ChatDatabaseRepository
import com.android.nextai.viewmodel.chat.entity.SessionGroup
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ViewModelScoped
class ChatSessionHolder @Inject constructor(
    val chatDatabaseRepository: ChatDatabaseRepository,
) {
    /**
     * Session state
     */
    private val _isLoading = MutableStateFlow(true)
    private val _isInSession = MutableStateFlow<Boolean>(false)

    val isLoading = _isLoading.asStateFlow()
    val isInSession: Boolean get() = _isInSession.value

    fun updateIsInSession(state: Boolean) {
        _isInSession.value = state
    }

    /**
     * Session info
     */
    private val _curSessionId = MutableStateFlow(-1L)

    val curSessionId = _curSessionId.asStateFlow()

    fun updateCurSessionId(id: Long) {
        _curSessionId.value = id
    }

    fun getCurSessionId(): Long {
        return curSessionId.value
    }

    /**
     * Session group
     */
    private val _groupedSessions =
        MutableStateFlow<Map<SessionGroup, List<SessionEntity>>>(emptyMap())
    private val _isBatchSelectMode = MutableStateFlow(false)
    private val _batchSelectedIdSet = MutableStateFlow<Set<Long>>(emptySet())

    val groupedSessions = _groupedSessions.asStateFlow()
    // Batch select mode, batch delete/pin sessions
    val isBatchSelectMode = _isBatchSelectMode.asStateFlow()
    val batchSelectedIdSet = _batchSelectedIdSet.asStateFlow()

    suspend fun loadSessions() {
        _isLoading.value = true
        val grouped = chatDatabaseRepository.getGroupSessions()
        _groupedSessions.value = grouped
        _isLoading.value = false
    }

    fun enterBatchSelectMode(sessionId: Long) {
        _isBatchSelectMode.value = true
        _batchSelectedIdSet.value = setOf(sessionId)
    }

    fun toggleItemSelect(sessionId: Long) {
        _batchSelectedIdSet.update { set ->
            if (set.contains(sessionId)) set - sessionId else set + sessionId
        }
    }

    fun exitBatchSelectMode() {
        _isBatchSelectMode.value = false
        _batchSelectedIdSet.value = emptySet()
    }

    // Group select or cancel
    fun toggleGroupSelect(
        sessions: List<SessionEntity>,
        isCheck: Boolean,
    ) {
        val ids = sessions.map { it.id }
        _batchSelectedIdSet.update { current ->
            if (isCheck) {
                current + ids
            } else {
                current - ids.toSet()
            }
        }
    }

    suspend fun batchDeleteSessions(idList: List<Long>) {
        chatDatabaseRepository.sessionDao.batchSoftDeleteSessions(1, idList)
        exitBatchSelectMode()
        loadSessions()
    }

    suspend fun batchPinSessions(idList: List<Long>) {
        chatDatabaseRepository.sessionDao.batchPinSessions(1, idList)
        exitBatchSelectMode()
        loadSessions()
    }

    suspend fun batchUnpinSessions(idList: List<Long>) {
        chatDatabaseRepository.sessionDao.batchUnpinSessions(0, idList)
        exitBatchSelectMode()
        loadSessions()
    }

    /**
     * Init for creating a new session
     */
    fun createSessionInit() {
        _isInSession.value = false
        _curSessionId.value = -1L
    }

    /**
     * Init for loading messages
     */
    fun loadMessagesInit(sessionId: Long) {
        _curSessionId.value = sessionId
        _isInSession.value = true
    }
}