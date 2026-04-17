package com.android.nextai.domain.database.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class SessionEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "ai_title")
    val aiTitle: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "system_prompt", defaultValue = "")
    val systemPrompt: String,

    @ColumnInfo(name = "token_count", defaultValue = "0")
    val tokenCount: Int,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Int,

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Int,
)
