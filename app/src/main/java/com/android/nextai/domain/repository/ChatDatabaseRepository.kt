package com.android.nextai.domain.repository

import androidx.room.withTransaction
import com.android.nextai.domain.database.db.ChatDatabase
import com.android.nextai.domain.database.db.dao.MessageDao
import com.android.nextai.domain.database.db.dao.SessionDao
import com.android.nextai.domain.database.db.entity.MessageEntity
import com.android.nextai.domain.database.db.entity.SessionEntity
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.entity.SessionGroup
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ChatDatabaseRepository @Inject constructor(
    val chatDB: ChatDatabase,
    val sessionDao: SessionDao,
    val messageDao: MessageDao
) {
    /**
     * Get all sessions and divide into different group
     */
    suspend fun getGroupSessions(): Map<SessionGroup, List<SessionEntity>>  {
        return sessionDao.getAllSessions().toGroup()
    }

    fun List<SessionEntity>.toGroup(): Map<SessionGroup, List<SessionEntity>> {
        val pinedList = mutableListOf<SessionEntity>()
        val todayList = mutableListOf<SessionEntity>()
        val weekList = mutableListOf<SessionEntity>()
        val monthList = mutableListOf<SessionEntity>()
        val earlierList = mutableListOf<SessionEntity>()

        val now = System.currentTimeMillis()

        val oneDay = 24 * 60 * 60 * 1000L
        val oneWeek = 7 * oneDay
        val oneMonth = 30 * oneDay
        val startOfToday = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfToday = startOfToday + oneDay

        for (session in this) {
            val uTime =session.updatedAt
            val diff = now - uTime
            when {
                session.isPinned == 1 -> pinedList.add(session)
                uTime >= startOfToday && uTime <= endOfToday -> todayList.add(session)
                diff <= oneWeek -> weekList.add(session)
                diff <= oneMonth -> monthList.add(session)
                else -> earlierList.add(session)
            }
        }

        return mapOf(
            SessionGroup.PIN to pinedList,
            SessionGroup.TODAY to todayList,
            SessionGroup.IN_WEEK to weekList,
            SessionGroup.IN_MONTH to monthList,
            SessionGroup.EARLIER to earlierList
        ).filterValues { it.isNotEmpty() }
    }

    suspend fun createSession(query: String): SessionEntity{
        val sessionEntity = SessionEntity(title = query)
        val id = sessionDao.insert(sessionEntity)
        val newSession = sessionEntity.copy(id = id)
        return newSession
    }

    /**
     * Create message
     */
    suspend fun createMessage(sessionId: Long, content: String, role: Role): MessageEntity{
        return chatDB.withTransaction {
            sessionDao.updateTime(sessionId, System.currentTimeMillis())
            val message = MessageEntity(
                sessionId = sessionId,
                role = role.name,
                content = content,
            )
            val msgId = messageDao.insert(message)
            return@withTransaction message.copy(id = msgId)
        }
    }

    /**
     * Create Session with user message
     */
    suspend fun createSessionWithUserMessage(query: String): Pair<SessionEntity, MessageEntity>{
        return chatDB.withTransaction {
            val session = createSession(query)
            val message = createMessage(session.id, query, Role.User)
            return@withTransaction Pair(session, message)
        }
    }

    /**
     * Update message content, used after streaming generation ends
     */
    suspend fun updateMessageContent(sessionId:Long, msgId: Long, content: String) {
        chatDB.withTransaction {
            sessionDao.updateTime(sessionId, System.currentTimeMillis())
            messageDao.updateMessageContent(msgId, content)
        }
    }

    /**
     * Pagination loads while removing messages with empty content
     */
    suspend fun getPageBefore(sessionId: Long, minMsgId: Long, pageSize:Int = 20): List<MessageEntity> {
        return chatDB.withTransaction {
            val messageList = messageDao.getPageBefore(sessionId, minMsgId, pageSize = pageSize)
            val emptyMessageIds = messageList
                .filter { it.content.isEmpty() }
                .map { it.id }
            if (emptyMessageIds.isNotEmpty()) {
                messageDao.deleteByIds(emptyMessageIds)
            }
            return@withTransaction messageList.filter { it.content.isNotEmpty() }
        }
    }
}
