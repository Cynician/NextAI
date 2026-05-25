package com.android.nextai.domain.remote.utils

import android.util.Log
import com.android.nextai.domain.remote.entity.ApiResult
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

class ModelManager(
    baseUrl: String,
    apiKey: String
) {
    companion object{
        private const val  TAG = "ModelManager"
    }

    private val client: OpenAIClient = OpenAIOkHttpClient.Companion.builder()
        .apiKey(apiKey)
        .baseUrl(baseUrl)
        .timeout(Duration.ofSeconds(15))
        .build()

    suspend fun checkModelExists(model: String): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                client.models().retrieve(model)
                ApiResult(success = true, message = "")
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.toString()
                Log.d(TAG, "error: $errorMsg")
                ApiResult(success = false, message = errorMsg)
            } finally {
                client.close()
            }
        }
    }
}