package com.android.nextai.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.android.nextai.data.datasource.datebase.room.ChatDatabase
import com.android.nextai.data.datasource.datebase.room.dao.MessageDao
import com.android.nextai.data.datasource.datebase.room.dao.ModelParamsDao
import com.android.nextai.data.datasource.datebase.room.dao.SessionDao
import com.android.nextai.data.datasource.datebase.room.entity.MessageEntity
import com.android.nextai.data.datasource.datebase.room.entity.ModelParamsEntity
import com.android.nextai.data.datasource.datebase.room.entity.SessionEntity
import com.android.nextai.data.datasource.datebase.room.entity.toDomain
import com.android.nextai.data.datasource.remote.AIDataSourceFactory
import com.android.nextai.data.datasource.remote.ApiType
import com.android.nextai.data.datasource.remote.utils.StreamBuffer
import com.android.nextai.domain.model.chat.Message
import com.android.nextai.domain.model.chat.MessageType
import com.android.nextai.domain.model.chat.ModelParams
import com.android.nextai.domain.model.chat.Session
import com.android.nextai.domain.model.provider.Provider
import com.android.nextai.domain.model.remote.GenerationEvent
import com.android.nextai.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class ChatRepositoryImpl @Inject constructor(
    private val chatDB: ChatDatabase,
    private val sessionDao: SessionDao,
    private val messageDao: MessageDao,
    private val modelParamsDao: ModelParamsDao,
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepository"
    }

    override fun streamingGen(
        apiType: ApiType,
        messageList: List<Message>,
        provider: Provider,
        modelId: String,
    ): Flow<GenerationEvent> =
        callbackFlow {
            val mdBuffer = StreamBuffer()
            val callback: (GenerationEvent) -> Unit = { event ->
                when (event) {
                    is GenerationEvent.Chunk -> {
                        for (char in event.content) {
                            val out = mdBuffer.process(char)
                            if (out != null) {
                                trySend(GenerationEvent.Chunk(out))
                            }
                        }
                    }

                    is GenerationEvent.Done,
                    is GenerationEvent.Error,
                        -> {
                        if (mdBuffer.holdBuffer.isNotEmpty()) {
                            trySend(GenerationEvent.Chunk(mdBuffer.holdBuffer.toString()))
                            mdBuffer.holdBuffer.clear()
                        }
                        trySend(event)
                        close()
                    }
                }
            }
            try {
                Log.d(TAG, "[streamingGen] start to streaming...")
                AIDataSourceFactory.createDataSource(apiType).getAIStreamingAnswer(
                    messageList = messageList,
                    provider = provider,
                    modelId = modelId,
                    generationCallback = callback
                )
            } catch (e: Exception) {
                trySend(GenerationEvent.Error("fail to streaming：${e.message}"))
                close()
            }
            awaitClose {
                Log.d(TAG, "[streamingGen] close...")
            }
        }.buffer(Channel.UNLIMITED)
            .onEach { delay(3) }
            .flowOn(Dispatchers.IO)

    /**
     * Get all sessions
     */
    override suspend fun getAllSessions(): List<Session> {
        val sessionEntityList = sessionDao.getAllSessions()
        val modelParamsIdList = sessionEntityList.map { it.modelParamsId }.distinct()
        val modelParamsMap = getModelParamsByIds(modelParamsIdList).associateBy { it.id }
        return sessionEntityList.map { entity ->
            val modelParams = modelParamsMap[entity.modelParamsId] ?: ModelParams()
            entity.toDomain(modelParams = modelParams)
        }
    }

    override suspend fun getSessionById(id: Long): Session? {
        val sessionEntity = sessionDao.getSessionById(id)
        if (sessionEntity == null) return null
        val modelParams = getModelParamsById(sessionEntity.modelParamsId)
        return sessionEntity.toDomain(modelParams)
    }

    override suspend fun getModelParamsByIds(ids: List<String>): List<ModelParams> {
        return modelParamsDao.getParamsByIds(ids).map {
            it.toDomain()
        }
    }

    override suspend fun deleteSessionsByIds(isSoftDelete: Boolean, ids: List<Long>) {
        if (isSoftDelete)
            sessionDao.batchSoftDeleteSessions(isDeleted = 1, idList = ids)
        else
            sessionDao.batchHardDeleteSessions(ids)
    }

    override suspend fun pinSessionsByIds(ids: List<Long>) {
        sessionDao.batchPinSessions(isPin = 1, idList = ids)
    }

    override suspend fun unpinSessionsByIds(ids: List<Long>) {
        sessionDao.batchUnpinSessions(isPin = 0, idList = ids)
    }

    override suspend fun createSession(
        query: String,
        modelParamsId: String,
        providerId: String,
        modelId: String,
    ): Session {
        val sessionEntity = SessionEntity(
            title = query,
            modelParamsId = modelParamsId,
            providerId = providerId,
            modelId = modelId,
        )
        val id = sessionDao.insert(sessionEntity)
        val modelParams = getModelParamsById(modelParamsId)
        return sessionEntity.copy(id = id).toDomain(modelParams)
    }

    override suspend fun createMessage(
        sessionId: Long,
        providerName: String,
        modelId: String,
        content: String,
        messageType: Int,
    ): Message {
        return chatDB.withTransaction {
            sessionDao.updateTime(sessionId, System.currentTimeMillis())
            val message = MessageEntity(
                sessionId = sessionId,
                providerName = providerName,
                modelId = modelId,
                type = messageType,
                content = content,
            )
            val msgId = messageDao.insert(message)
            return@withTransaction message.copy(id = msgId).toDomain()
        }
    }

    override suspend fun createSessionWithUserMessage(
        query: String,
        modelParamsId: String,
        provider: Provider,
        modelId: String,
    ): Pair<Session, Message> {
        return chatDB.withTransaction {

            val session = createSession(
                query = query,
                modelParamsId = modelParamsId,
                providerId = provider.id,
                modelId = modelId
            )
            val message = createMessage(
                sessionId = session.id,
                providerName = provider.name,
                modelId = modelId,
                content = query,
                messageType = MessageType.USER,
            )
            return@withTransaction Pair(session, message)
        }
    }

    override suspend fun getModelParamsById(id: String): ModelParams {
        return chatDB.withTransaction {
            val entity = modelParamsDao.getParamsById(id)
            if (entity != null) {
                entity.toDomain()
            } else {
                val defaultEntity = ModelParamsEntity()
                modelParamsDao.insert(defaultEntity)
                defaultEntity.toDomain()
            }
        }

    }

    override suspend fun updateMessage(
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
    ) {
        chatDB.withTransaction {
            sessionDao.updateTime(sessionId, System.currentTimeMillis())
            messageDao.updateMessageFields(
                id,
                providerName,
                modelId,
                reasoningContent,
                content,
                status,
                tokenCount,
                extra,
                updatedAt
            )
        }
    }

    override suspend fun updateMessageContent(sessionId: Long, msgId: Long, content: String) {
        chatDB.withTransaction {
            sessionDao.updateTime(sessionId, System.currentTimeMillis())
            messageDao.updateMessageContent(msgId, content)
        }
    }

    override suspend fun getLastPageMessages(
        sessionId: Long,
        minMsgId: Long,
        pageSize: Int,
    ): List<Message> {
        return chatDB.withTransaction {
            val messageList =
                messageDao.getLastPageMessages(sessionId, minMsgId, pageSize = pageSize)
            val emptyMessageIds = messageList
                .filter { it.content.isEmpty() }
                .map { it.id }
            if (emptyMessageIds.isNotEmpty()) {
                messageDao.deleteByIds(emptyMessageIds)
            }
            return@withTransaction messageList.filter { it.content.isNotEmpty() }.map {
                it.toDomain()
            }
        }
    }
}