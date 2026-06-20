package com.android.nextai.data.datasource.datebase.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.nextai.domain.model.chat.ModelParams

@Entity(
    tableName = "model_params",
)
data class ModelParamsEntity(
    @PrimaryKey
    @ColumnInfo("id", defaultValue = "0")
    val id: String = "0",

    @ColumnInfo(name = "temperature", defaultValue = "0.7")
    val temperature: Float = 0.7f,

    @ColumnInfo(name = "top_p", defaultValue = "0.9")
    val topP: Float = 0.9f,

    @ColumnInfo(name = "top_k", defaultValue = "40")
    val topK: Int = 40,

    @ColumnInfo(name = "max_output_tokens", defaultValue = "2048")
    val maxOutputTokens: Int = 2048,

    @ColumnInfo(name = "presence_penalty", defaultValue = "0.0")
    val presencePenalty: Float = 0.0f,

    @ColumnInfo(name = "frequency_penalty", defaultValue = "0.0")
    val frequencyPenalty: Float = 0.0f,

    @ColumnInfo(name = "extra", defaultValue = "")
    val extra: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)


fun ModelParamsEntity.toDomain() = ModelParams(
    id = this.id,
    temperature = this.temperature,
    topP = this.topP,
    topK = this.topK,
    maxOutputTokens = this.maxOutputTokens,
    presencePenalty = this.presencePenalty,
    frequencyPenalty = this.frequencyPenalty,
    extra = this.extra,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)