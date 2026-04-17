package com.android.nextai.domain.database.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message",
    indices = [
        Index(value = ["session_id"])
    ]
)
data class MessageEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long = 0,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "role")
    val role: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "status", defaultValue = "0")
    val status: Int,

    @ColumnInfo(name = "token_count", defaultValue = "0")
    val tokenCount: Int,

    @ColumnInfo(name = "extra", defaultValue = "")
    val extra: String
)