package com.android.nextai.domain.remote.test

import android.util.Log
import com.android.nextai.domain.database.sqlite.entity.MessageEntity
import com.android.nextai.domain.remote.AIModelDataSource
import com.android.nextai.domain.remote.entity.GenerationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


object TestRemoteDataSource : AIModelDataSource {
    private const val TAG = "TestRemoteDataSource"
    private var generationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun getAIAnswer(messageList: List<MessageEntity>): String {
        return TestData.getData()
    }

    override suspend fun getAIStreamingAnswer(
        messageList: List<MessageEntity>,
        callback: (GenerationEvent) -> Unit,
    ) {
        val testData = TestData.getData()
//        val testData = "123456789"
        try {
            val chunks = mutableListOf<String>()
            var currentIndex = 0

            while (currentIndex < testData.length) {
                // generate streaming chunk
                val remainingLength = testData.length - currentIndex
                val chunkSize = Random.nextInt(1, minOf(51, remainingLength + 1))

                val chunk = testData.substring(currentIndex, currentIndex + chunkSize)
                chunks.add(chunk)
                currentIndex += chunkSize
            }
            generationScope.launch {
                chunks.forEach { chunk ->
                    delay(Random.nextLong(20, 50))
                    callback(GenerationEvent.Word(chunk))
                    Log.i(TAG, chunk)
            }
            callback(GenerationEvent.Done)
        }

        } catch (e: Exception) {
            Log.e(TAG, "getAIStreamingAnswer# error:${e}")
        }

    }

}
