package com.android.nextai.data.remote

import com.android.nextai.data.remote.openai.OpenAIRemoteDataSource

enum class ApiType{
    OPENAI,
    OTHER
}

object AIFactory {
    fun createAIModel(apiType: ApiType): AIModelDataSource{
        return when(apiType){
            ApiType.OPENAI -> OpenAIRemoteDataSource
            ApiType.OTHER -> TODO()
        }
    }
}