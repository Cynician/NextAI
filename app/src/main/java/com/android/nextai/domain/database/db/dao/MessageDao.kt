package com.android.nextai.domain.database.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.nextai.domain.database.db.entity.MessageEntity


@Dao
interface MessageDao {
    /**
     * insert new message
     */
    @Insert
    suspend fun insert(message: MessageEntity): Long

    /**
     * pagination (upload history)
     */
    @Query("""
        SELECT * FROM message
        WHERE session_id = :sessionId
        AND id < :id
        ORDER BY id DESC
        LIMIT :limit
    """)
    suspend fun getMessagesBefore(
        sessionId: Long,
        id: Long,
        limit: Int = 20
    ): List<MessageEntity>

    /**
     * update status(sending:0, success:1, failure:2)
     */
    @Query("""
        UPDATE message
        SET status = :status
        WHERE id = :id
    """)
    suspend fun updateStatus(id: Long, status: Int)

    /**
     * delete message
     */
    @Query("DELETE FROM message WHERE id = :id")
    suspend fun delete(id: Long)

}