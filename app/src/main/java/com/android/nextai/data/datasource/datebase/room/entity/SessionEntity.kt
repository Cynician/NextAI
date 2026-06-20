package com.android.nextai.data.datasource.datebase.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.android.nextai.domain.model.chat.ModelParams
import com.android.nextai.domain.model.chat.Session

@Entity(
    tableName = "session",
    indices = [
        Index(value = ["provider_id"]),
    ]
)
data class SessionEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Long = 0,

    @ColumnInfo("provider_id")
    val providerId: String = "",

    @ColumnInfo("model_id")
    val modelId: String = "",

    @ColumnInfo("model_params_id")
    val modelParamsId: String = "",

    @ColumnInfo(name = "title", defaultValue = "")
    val title: String = "",

    @ColumnInfo(name = "ai_title")
    val aiTitle: String = "",

    @ColumnInfo(name = "system_prompt", defaultValue = "")
    val systemPrompt: String = "解答用户问题",

    @ColumnInfo(name = "token_count", defaultValue = "0")
    val tokenCount: Int = 0,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Int = 0,

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)

fun SessionEntity.toDomain(
    modelParams: ModelParams,
) = Session(
    id = this.id,
    providerId = this.providerId,
    modelId = this.modelId,
    modelParams = modelParams,
    title = this.title,
    aiTitle = this.aiTitle,
    systemPrompt = this.systemPrompt,
    tokenCount = this.tokenCount,
    isPinned = this.isPinned,
    isDeleted = this.isDeleted,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)