package com.android.nextai.data.datasource.remote

import com.android.nextai.data.datasource.remote.openai.OpenAIRemoteDataSource


enum class ApiType {
    OPENAI,
    OTHER
}

object AIDataSourceFactory {
    fun createDataSource(apiType: ApiType): AIDataSource {
        return when (apiType) {
            ApiType.OPENAI -> OpenAIRemoteDataSource
            ApiType.OTHER -> TODO()
        }
    }
}