package com.android.nextai.domain.database.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.nextai.domain.database.db.entity.SessionEntity

@Dao
interface SessionDao {
    /**
     * create new session
     */
    @Insert
    suspend fun insert(session: SessionEntity): Long

    /**
     * get all sessions
     */
    @Query("""
        SELECT * FROM session
        WHERE is_deleted = 0
        ORDER BY is_pinned DESC, id DESC
    """)
    suspend fun getSessions(): List<SessionEntity>

    /**
     * update time
     */
    @Query("""
        UPDATE session
        SET updated_at = :time
        WHERE id = :sessionId
    """)
    suspend fun updateTime(sessionId: Long, time: Long)

    /**
     * update ai generated title
     */
    @Query("""
        UPDATE session
        SET ai_title = :title
        WHERE id = :sessionId
    """)
    suspend fun updateAiTitle(sessionId: Long, title: String)

    /**
     * soft delete
     */
    @Query("""
        UPDATE session
        SET is_deleted = 1
        WHERE id = :sessionId
    """)
    suspend fun softDelete(sessionId: Long)

    /**
     * pinned
     */
    @Query("""
        UPDATE session
        SET is_pinned = :pinned
        WHERE id = :sessionId
    """)
    suspend fun pinSession(sessionId: Long, pinned: Int)
}