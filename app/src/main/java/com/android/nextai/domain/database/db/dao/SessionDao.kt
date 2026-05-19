package com.android.nextai.domain.database.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.nextai.domain.database.db.entity.SessionEntity

@Dao
interface SessionDao {
    /**
     * Create new session
     */
    @Insert
    suspend fun insert(session: SessionEntity): Long

    /**
     * Get all sessions
     */
    @Query(
        """
        SELECT * FROM session
        WHERE is_deleted = 0
        ORDER BY is_pinned DESC, updated_at DESC
    """
    )
    suspend fun getAllSessions(): List<SessionEntity>

    /**
     * Update time
     */
    @Query(
        """
        UPDATE session
        SET updated_at = :time
        WHERE id = :sessionId
    """
    )
    suspend fun updateTime(sessionId: Long, time: Long)

    /**
     * Update ai generated title
     */
    @Query(
        """
        UPDATE session
        SET ai_title = :title,
            updated_at = strftime('%s','now') * 1000
        WHERE id = :sessionId
    """
    )
    suspend fun updateAiTitle(sessionId: Long, title: String)

    /**
     * Soft delete session
     */
    @Query(
        """
        UPDATE session
        SET is_deleted = 1,
            updated_at = strftime('%s','now') * 1000
        WHERE id = :sessionId
    """
    )
    suspend fun softDelete(sessionId: Long)

    /**
     * Batch soft delete sessions
     */
    @Query(
        """
        UPDATE session
        SET is_deleted = :isDeleted,
            updated_at = strftime('%s','now') * 1000
        WHERE id IN (:idList)
    """
    )
    suspend fun batchSoftDeleteSessions(isDeleted: Int, idList: List<Long>)


    /**
     * Batch pin sessions
     */
    @Query(
        """
        UPDATE session
        SET is_pinned = :isPin, 
            updated_at = strftime('%s','now') * 1000
        WHERE id IN (:idList)
    """
    )
    suspend fun batchPinSessions(isPin: Int, idList: List<Long>)

    /**
     * Batch unpin sessions
     */
    @Query(
        """
            UPDATE session
            SET is_pinned = :isPin, 
            updated_at = strftime('%s','now') * 1000
        WHERE id in (:idList)
        """
    )
    suspend fun batchUnpinSessions(isPin: Int, idList: List<Long>)
}