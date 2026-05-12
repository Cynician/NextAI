package com.android.nextai.domain.repository

import com.android.nextai.domain.database.db.dao.MessageDao
import com.android.nextai.domain.database.db.dao.SessionDao
import com.android.nextai.domain.database.db.entity.MessageEntity
import com.android.nextai.domain.database.db.entity.SessionEntity
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.entity.SessionGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ChatDatabaseRepository @Inject constructor(
    val sessionDao: SessionDao,
    val messageDao: MessageDao
) {

    suspend fun createSession(query: String): SessionEntity{
        return withContext(Dispatchers.IO){
            val now = System.currentTimeMillis()
            val sessionEntity = SessionEntity(
                title = query,
                aiTitle = "",
                createdAt = now,
                updatedAt = now,
                systemPrompt = "解答用户问题",
                tokenCount = 0,
                isPinned = 0,
                isDeleted = 0)
            val id = sessionDao.insert(sessionEntity)
            val newSession = sessionEntity.copy(id = id)
            return@withContext newSession
        }
    }

    /**
     * insert pairs of Q&A messages
     */
    suspend fun insertPairMessage(sessionId:Long, query:String, response:String):Pair<Long, Long>{
        return withContext(Dispatchers.IO){
            val userEntity = MessageEntity(
                sessionId = sessionId,
                role = Role.User.name,
                content = query,
                createdAt = System.currentTimeMillis(),
                status = 1,
                tokenCount = query.length,
                extra = ""
            )
            val assistantEntity = MessageEntity(
                sessionId = sessionId,
                role = Role.Assistant.name,
                content = response,
                createdAt = System.currentTimeMillis(),
                status = 1,
                tokenCount = response.length,
                extra = ""
            )
            val userMessageId = messageDao.insert(userEntity)
            val assistantMessageId = messageDao.insert(assistantEntity)
            return@withContext Pair(userMessageId, assistantMessageId)
        }
    }

    /**
     * get all sessions and divide into different group
     */
    suspend fun getAllSessions(): Map<SessionGroup, List<SessionEntity>>  {
        return withContext(Dispatchers.IO){
            return@withContext sessionDao.getAllSessions().toGroup()
        }
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
            SessionGroup.PINNED to pinedList,
            SessionGroup.TODAY to todayList,
            SessionGroup.IN_WEEK to weekList,
            SessionGroup.IN_MONTH to monthList,
            SessionGroup.EARLIER to earlierList
        ).filterValues { it.isNotEmpty() }
    }
}
