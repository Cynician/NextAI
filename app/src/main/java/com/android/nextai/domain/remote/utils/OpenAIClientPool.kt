package com.android.nextai.domain.remote.utils

import android.util.Log
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


object OpenAIClientPool {
    private const val TAG = "OpenAIClientPool"

    /**
     * key:
     *      baseUrl#apiKey
     */
    private val clientPool = ConcurrentHashMap<String, OpenAIClient>()

    /**
     * Get client
     */
    fun getClient(
        baseUrl: String,
        apiKey: String,
    ): OpenAIClient {

        val key = buildKey(baseUrl = baseUrl, apiKey = apiKey,)
        clientPool[key]?.let { return it }
        // Create new client
        val newClient =
            try {
                OpenAIOkHttpClient.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .maxIdleConnections(10)
                    .keepAliveDuration(Duration.ofMinutes(5))
                    .timeout(Duration.ofMinutes(10))
                    .maxRetries(2)
                    .build()
            } catch (e: Exception) {
                Log.e(TAG, "error: ${e.message}")
                throw RuntimeException(
                    "Create OpenAIClient failed: ${e.message}",
                    e,
                )
            }
        return clientPool.putIfAbsent(key, newClient)
            ?: newClient
    }

    /**
     * Client cache num
     */
    fun size(): Int {
        return clientPool.size
    }

    /**
     * Key
     */
    private fun buildKey(
        baseUrl: String,
        apiKey: String,
    ): String {
        return "${baseUrl}#${apiKey}"
    }
}