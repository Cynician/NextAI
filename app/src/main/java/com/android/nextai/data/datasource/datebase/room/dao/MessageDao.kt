package com.android.nextai.data.datasource.datebase.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.nextai.data.datasource.datebase.room.entity.MessageEntity


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
    suspend fun getLastPageMessages(
        sessionId: Long,
        minMsgId: Long,
        pageSize: Int = 20
    ): List<MessageEntity>


    /**
     * Delete message
     */
    @Query("DELETE FROM message WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
    UPDATE message
    SET
        provider_name = IFNULL(:providerName, provider_name),
        model_name = IFNULL(:modelId, model_name),
        reasoning_content = IFNULL(:reasoningContent, reasoning_content),
        content = IFNULL(:content, content),
        status = IFNULL(:status, status),
        token_count = IFNULL(:tokenCount, token_count),
        extra = IFNULL(:extra, extra),
        updated_at = IFNULL(:updatedAt, updated_at)
    WHERE id = :id
""")
    suspend fun updateMessageFields(
        id: Long,
        providerName: String?,
        modelId: String?,
        reasoningContent: String?,
        content: String?,
        status: Int?,
        tokenCount: Int?,
        extra: String?,
        updatedAt: Long?
    )

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