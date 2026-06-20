package com.android.nextai.data.datasource.datebase.datastore.entity

import com.android.nextai.domain.model.provider.Model
import kotlinx.serialization.Serializable

@Serializable
data class ModelEntity(
    val id: String,  // id is model name here
    val owner: String,
    val createdAt: Long,
)

fun ModelEntity.toDomain(): Model = Model(
    id = this.id,
    owner = this.owner,
    createdAt = this.createdAt
)

fun Model.toEntity(): ModelEntity = ModelEntity(
    id = this.id,
    owner = this.owner,
    createdAt = this.createdAt
)