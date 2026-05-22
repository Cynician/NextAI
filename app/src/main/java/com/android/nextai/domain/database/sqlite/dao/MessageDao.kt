package com.android.nextai.domain.database.sqlite.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.nextai.domain.database.sqlite.entity.MessageEntity


@Dao
interface MessageDao {
    /**
     * Insert new message
     */
    @Insert
    suspend fun insert(message: MessageEntity): Long

    /**
     * Pagination (upload history)
     */
    @Query("""
        SELECT * FROM message
        WHERE session_id = :sessionId
        AND id < :minMsgId
        ORDER BY id DESC
        LIMIT :pageSize
    """)
    suspend fun getPageBefore(
        sessionId: Long,
        minMsgId: Long,
        pageSize: Int = 20
    ): List<MessageEntity>

    /**
     * Update status(sending:0, success:1, failure:2)
     */
    @Query("""
        UPDATE message
        SET status = :status
        WHERE id = :id
    """)
    suspend fun updateStatus(id: Long, status: Int)

    /**
     * Delete message
     */
    @Query("DELETE FROM message WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * Update message content
     */
    @Query("""
        UPDATE message
        SET content = :content
        WHERE id = :id
    """)
    suspend fun updateMessageContent(id: Long, content: String)

    /**
     * Delete by id list
     */
    @Query("""
    DELETE FROM message
    WHERE id IN (:ids)
""")
    suspend fun deleteByIds(ids: List<Long>)
}