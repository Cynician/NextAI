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
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "system_prompt", defaultValue = "")
    val systemPrompt: String = "解答用户问题",

    @ColumnInfo(name = "token_count", defaultValue = "0")
    val tokenCount: Int = 0,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Int = 0,

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Int = 0,
)
