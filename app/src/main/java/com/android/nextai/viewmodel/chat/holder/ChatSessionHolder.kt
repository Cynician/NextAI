package com.android.nextai.viewmodel.chat.holder

import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.Session
import com.android.nextai.domain.usecase.chat.BatchDeleteSessionsUseCase
import com.android.nextai.domain.usecase.chat.BatchPinSessionsUseCase
import com.android.nextai.domain.usecase.chat.GetBatchUnpinSessionsUseCase
import com.android.nextai.domain.usecase.chat.GetGroupSessionsUseCase
import com.android.nextai.viewmodel.chat.state.ChatSessionState
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ViewModelScoped
class ChatSessionHolder @Inject constructor(
    private val getGetGroupSessionsUseCase: GetGroupSessionsUseCase,
    private val batchDeleteSessionsUseCase: BatchDeleteSessionsUseCase,
    private val batchPinSessionsUseCase: BatchPinSessionsUseCase,
    private val getBatchUnpinSessionsUseCase: GetBatchUnpinSessionsUseCase,
) {
    // --- Session States & Messages Management ---

    /**
     * Core State
     *
     * key: session id, v: state.
     */
    private val _sessionStates = MutableStateFlow<Map<Long, ChatSessionState>>(emptyMap())
    val sessionStates = _sessionStates.asStateFlow()

    /**
     * Event flow to trigger UI scrolling to the latest message for a session.
     */
    private val _scrollToLatestMessageEvent = MutableSharedFlow<Long>()
    val scrollToLatestMessageEvent = _scrollToLatestMessageEvent.asSharedFlow()


    // --- Global Session UI Status ---

    /**
     * Indicates if the history sessions is currently being fetched from DB and being grouped by time.
     */
    private val _isLoadingSessions = MutableStateFlow(true)
    val isLoadingSessions = _isLoadingSessions.asStateFlow()

    /**
     * Tracks if user is actively in a valid session (true) or starting a brand new one (false).
     */
    private val _isInSession = MutableStateFlow(false)
    val isInSession: Boolean get() = _isInSession.value

    /**
     * Is the first page of messages loading?
     */
    private val _isLoadingFirst = MutableStateFlow(false)
    val isLoadingFirst = _isLoadingFirst.asStateFlow()


    // --- Current Active Session ---

    /**
     * ID of the currently opened chat session (-1L means none or a pending new session).
     */
    private val _curSessionId = MutableStateFlow(-1L)
    val curSessionId = _curSessionId.asStateFlow()


    // --- Session List & Batch Selection ---

    /**
     * Chat sessions grouped by time periods for history UI.
     */
    private val _groupedSessions =
        MutableStateFlow<Map<Int, List<Session>>>(emptyMap())
    val groupedSessions = _groupedSessions.asStateFlow()

    /**
     * Flags whether the user is in edit mode to select multiple sessions.
     */
    private val _isBatchSelectMode = MutableStateFlow(false)
    val isBatchSelectMode = _isBatchSelectMode.asStateFlow()

    /**
     * Stores the IDs of all currently selected sessions for batch actions.
     */
    private val _batchSelectedIdSet = MutableStateFlow<Set<Long>>(emptySet())
    val batchSelectedIdSet = _batchSelectedIdSet.asStateFlow()


    // --------------------------------------------------------------------------------------------//

    // --- Init ---

    fun initSession() {
        _isInSession.value = false
        _curSessionId.value = -1L
    }

    fun onLoadMessagesSuccess(sessionId: Long) {
        _curSessionId.value = sessionId
        _isInSession.value = true
    }

    // --- SessionState Update Helpers ---

    private fun updateState(sessionId: Long, transform: (ChatSessionState) -> ChatSessionState) {
        _sessionStates.update { map ->
            val currentState = map[sessionId] ?: ChatSessionState(sessionId = sessionId)
            map + (sessionId to transform(currentState))
        }
    }

    fun getState(sessionId: Long): ChatSessionState {
        return _sessionStates.value[sessionId] ?: ChatSessionState(sessionId = sessionId)
    }


    fun setMessages(sessionId: Long, messages: List<Message>) {
        updateState(sessionId) { it.copy(messageList = messages) }
    }

    fun addMessage(sessionId: Long, newM: Message) {
        updateState(sessionId) { it.copy(messageList = it.messageList + newM) }
    }

    fun updateLastestMessage(sessionId: Long, newM: Message) {
        updateState(sessionId) { state ->
            if (state.messageList.isEmpty()) state
            else state.copy(
                messageList = state.messageList.toMutableList().apply { this[lastIndex] = newM })
        }
    }

    fun addMessagesToHead(sessionId: Long, newMessages: List<Message>) {
        updateState(sessionId) { it.copy(messageList = newMessages + it.messageList) }
    }

    fun removeTailMessage(sessionId: Long, messageId: Long) {
        updateState(sessionId) { state ->
            state.copy(messageList = state.messageList.filter { it.id < messageId })
        }
    }

    fun updateIsTextStreaming(sessionId: Long, state: Boolean) {
        updateState(sessionId) { it.copy(isTextGenerating = state) }
    }

    fun updateCurResponse(sessionId: Long, delta: String) {
        updateState(sessionId) { it.copy(curResponse = it.curResponse + delta) }
    }

    fun clearCurrentResponse(sessionId: Long) {
        updateState(sessionId) { it.copy(curResponse = "") }
    }

    fun updateCurMessagesMinId(sessionId: Long, id: Long) {
        updateState(sessionId) { it.copy(curMessagesMinId = id) }
    }

    fun updateHasMoreMessages(sessionId: Long, value: Boolean) {
        updateState(sessionId) { it.copy(hasMoreMessages = value) }
    }

    fun removeSession(sessionId: Long) {
        _sessionStates.update { it - sessionId }
    }

    suspend fun emitScrollToLatestMessageEvent(sessionId: Long) {
        _scrollToLatestMessageEvent.emit(sessionId)
    }


    // --- Global State ---

    fun updateIsInSession(state: Boolean) {
        _isInSession.update { state }
    }

    fun updateCurSessionId(id: Long) {
        _curSessionId.value = id
    }

    fun getCurSessionId(): Long {
        return curSessionId.value
    }

    fun updateIsLoadingFirst(state: Boolean) {
        _isLoadingFirst.value = state
    }

    suspend fun loadingSessions() {
        _isLoadingSessions.value = true
        _groupedSessions.value = getGetGroupSessionsUseCase().getOrThrow()
        _isLoadingSessions.value = false
    }

    // --- Batch Select Mode ---

    fun onEnterBatchSelectMode(sessionId: Long) {
        _isBatchSelectMode.value = true
        _batchSelectedIdSet.value = setOf(sessionId)
    }

    fun onToggleSessionSelectState(sessionId: Long) {
        _batchSelectedIdSet.update { set ->
            if (set.contains(sessionId)) set - sessionId else set + sessionId
        }
    }

    fun onExitBatchSelectMode() {
        _isBatchSelectMode.value = false
        _batchSelectedIdSet.value = emptySet()
    }

    fun onToggleGroupSelectState(sessions: List<Session>, isCheck: Boolean) {
        val ids = sessions.map { it.id }
        _batchSelectedIdSet.update { current ->
            if (isCheck) current + ids else current - ids.toSet()
        }
    }

    suspend fun onBatchDeleteSessions(isSoftDelete: Boolean = true, idList: List<Long>) {
        batchDeleteSessionsUseCase(isSoftDelete, idList).getOrThrow()
        onExitBatchSelectMode()
        loadingSessions()
    }

    suspend fun onBatchPinSessions(idList: List<Long>) {
        batchPinSessionsUseCase(idList).getOrThrow()
        onExitBatchSelectMode()
        loadingSessions()
    }

    suspend fun onBatchUnpinSessions(idList: List<Long>) {
        getBatchUnpinSessionsUseCase(idList).getOrThrow()
        onExitBatchSelectMode()
        loadingSessions()
    }
}