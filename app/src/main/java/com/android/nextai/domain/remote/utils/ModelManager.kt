package com.android.nextai.domain.remote.utils

import android.util.Log
import com.android.nextai.domain.database.datastore.entity.ModelEntity
import com.android.nextai.domain.remote.entity.ApiResult
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

    suspend fun retrievingModels(): ApiResult<List<ModelEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val models = client.models()
                    .list()
                    .items()
                    .map {
                        ModelEntity(
                            id = it.id(),
                            owner = it.ownedBy(),
                            created = it.created()
                        )
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