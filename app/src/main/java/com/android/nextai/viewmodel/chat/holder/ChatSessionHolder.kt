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
    val chatDatabaseRepository: ChatDatabaseRepository
) {
    /**
     * Session state
     */
    private val _isLoading = MutableStateFlow(true)
    private val _isInSession = MutableStateFlow<Boolean>(false)

    val isLoading = _isLoading.asStateFlow()
    val isInSession = _isInSession.asStateFlow()

    fun getIsInSession(): Boolean{
        return isInSession.value
    }

    fun updateIsInSession(state: Boolean){
        _isInSession.value = state
    }

    /**
     * Session info
     */
    private val _curSessionId = MutableStateFlow(-1L)
    val curSessionId = _curSessionId.asStateFlow()
    fun updateCurSessionId(id: Long){
        _curSessionId.value = id
    }
    fun getCurSessionId(): Long{
        return curSessionId.value
    }

    /**
     * Session group
     */
    private val _groupedSessions = MutableStateFlow<Map<SessionGroup, List<SessionEntity>>>(emptyMap())
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedIdSet = MutableStateFlow<Set<Long>>(emptySet())

    val groupedSessions = _groupedSessions.asStateFlow()
    // Bulk selection mode for bulk delete or pin operations of sessions
    val isSelectionMode = _isSelectionMode.asStateFlow()
    val selectedIdSet = _selectedIdSet.asStateFlow()

    suspend fun loadSessions() {
        _isLoading.value = true
        val grouped = chatDatabaseRepository.getGroupSessions()
        _groupedSessions.value = grouped
        _isLoading.value = false
    }

    fun enterSelectionMode(sessionId: Long) {
        _isSelectionMode.value = true
        _selectedIdSet.value = setOf(sessionId)
    }

    fun toggleSelection(sessionId: Long) {
        _selectedIdSet.update { set ->
            if (set.contains(sessionId)) set - sessionId else set + sessionId
        }
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedIdSet.value = emptySet()
    }

    // Group selection/Ungroup selection
    fun toggleGroupSelection(
        sessions: List<SessionEntity>,
        isCheck: Boolean
    ) {
        val ids = sessions.map { it.id }
        _selectedIdSet.update { current ->
            if (isCheck) {
                current + ids
            } else {
                current - ids.toSet()
            }
        }
    }

    suspend fun batchDeleteSessions(idList:List<Long>){
        chatDatabaseRepository.sessionDao.batchSoftDeleteSessions(1, idList)
        exitSelectionMode()
        loadSessions()
    }

    suspend fun batchPinSessions(idList:List<Long>){
        chatDatabaseRepository.sessionDao.batchPinSessions(1, idList)
        exitSelectionMode()
        loadSessions()
    }

    suspend fun unpinnedSession(id:Long){
        chatDatabaseRepository.sessionDao.unpinnedSession(0, id)
        loadSessions()
    }

    /**
     * Initialization method when creating a new session or selecting a different session
     */
    fun init() {
        _isInSession.value = false
        _curSessionId.value = -1L
    }
}