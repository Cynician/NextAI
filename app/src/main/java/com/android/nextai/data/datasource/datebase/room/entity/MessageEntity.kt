package com.android.nextai.data.datasource.datebase.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.nextai.domain.model.chat.Message

@Entity(
    tableName = "message",
    indices = [
        Index(value = ["session_id"]),
    ]
)
data class MessageEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long = 0,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo("provider_name")
    val providerName: String = "",

    @ColumnInfo("model_name")
    val modelId: String = "",

    @ColumnInfo(name = "type")
    val type: Int,

    @ColumnInfo(name = "reasoning_content", defaultValue = "")
    val reasoningContent: String = "",

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "status", defaultValue = "0")
    val status: Int = 0,

    @ColumnInfo(name = "token_count", defaultValue = "0")
    val tokenCount: Int = 0,

    @ColumnInfo(name = "extra", defaultValue = "")
    val extra: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        sessionId = sessionId,
        providerName = providerName,
        modelId = modelId,
        type = type,
        reasoningContent = reasoningContent,
        content = content,
        status = status,
        tokenCount = tokenCount,
        extra = extra,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

//fun Message.toEntity(providerName: String): MessageEntity {
//    return MessageEntity(
//        id = id,
//        sessionId = sessionId,
//        providerName = providerName,
//        modelId = modelId,
//        type = type,
//        reasoningContent = reasoningContent,
//        content = content,
//        status = status,
//        tokenCount = tokenCount,
//        extra = extra,
//        createdAt = createdAt,
//        updatedAt = updatedAt,
//    )
//}