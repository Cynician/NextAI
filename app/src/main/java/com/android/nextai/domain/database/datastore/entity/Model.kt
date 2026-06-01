package com.android.nextai.domain.database.datastore.entity

import kotlinx.serialization.Serializable

@Serializable
data class ModelEntity(
    val id: String,  // id is model name here
    val owner: String,
    val created: Long,
)