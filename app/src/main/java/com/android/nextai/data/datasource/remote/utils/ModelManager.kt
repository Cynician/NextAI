package com.android.nextai.data.datasource.remote.utils

import android.util.Log
import com.android.nextai.data.datasource.datebase.datastore.entity.ModelEntity
import com.android.nextai.data.datasource.datebase.datastore.entity.toDomain
import com.android.nextai.domain.model.provider.Model
import com.android.nextai.domain.model.remote.ApiResult
import com.openai.client.OpenAIClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModelManager(
    baseUrl: String,
    apiKey: String,
) {
    companion object {
        private const val TAG = "ModelManager"
    }

    private val client: OpenAIClient =
        OpenAIClientPool.getClient(baseUrl = baseUrl, apiKey = apiKey)

    suspend fun retrievingModels(): ApiResult<List<Model>> {
        return withContext(Dispatchers.IO) {
            try {
                val models = client.models()
                    .list()
                    .items()
                    .map {
                        ModelEntity(
                            id = it.id(),
                            owner = it.ownedBy(),
                            createdAt = it.created()
                        ).toDomain()
                    }
                Log.d(TAG, "models: $models")
                ApiResult.Success(models)

            } catch (e: Exception) {
                Log.e(TAG, "error:", e)
                ApiResult.Error(
                    message = e.message ?: "Unknown error",
                    throwable = e
                )
            }
        }
    }
}