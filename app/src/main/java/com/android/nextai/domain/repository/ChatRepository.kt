package com.android.nextai.domain.repository

import com.android.nextai.data.datasource.remote.ApiType
import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.ModelParams
import com.android.nextai.domain.model.chat.Session
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent
import kotlinx.coroutines.flow.Flow


interface ChatRepository {

    fun streamingGen(
        apiType: ApiType,
        messageList: List<Message>,
        provider: Provider,
        modelId: String,
    ): Flow<GenerationEvent>

    suspend fun getAllSessions(): List<Session>

    suspend fun getSessionById(id: Long): Session?

    suspend fun getModelParamsByIds(ids: List<String>): List<ModelParams>

    suspend fun deleteSessionsByIds(isSoftDelete: Boolean = true, ids: List<Long>)

    suspend fun pinSessionsByIds(ids: List<Long>)

    suspend fun unpinSessionsByIds(ids: List<Long>)

    suspend fun createSession(
        query: String,
        modelParamsId: String,
        providerId: String,
        modelId: String,
    ): Session

    suspend fun createMessage(
        sessionId: Long,
        providerName: String,
        modelId: String,
        content: String,
        messageType: Int,
    ): Message

    suspend fun createSessionWithUserMessage(
        query: String,
        modelParamsId: String,
        provider: Provider,
        modelId: String,
    ): Pair<Session, Message>

    suspend fun getModelParamsById(id: String): ModelParams

    suspend fun updateMessage(
        id: Long,
        sessionId: Long,
        providerName: String?,
        modelId: String?,
        reasoningContent: String?,
        content: String?,
        status: Int?,
        tokenCount: Int?,
        extra: String?,
        updatedAt: Long?,
    )

    suspend fun updateMessageContent(sessionId: Long, msgId: Long, content: String)

    suspend fun getLastPageMessages(
        sessionId: Long,
        minMsgId: Long,
        pageSize: Int = 10,
    ): List<Message>
}