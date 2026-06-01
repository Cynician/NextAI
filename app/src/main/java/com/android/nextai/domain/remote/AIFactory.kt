package com.android.nextai.domain.remote

import com.android.nextai.domain.remote.qianwen.OpenAIRemoteDataSource
import com.android.nextai.domain.remote.test.TestRemoteDataSource

enum class ApiType{
    OPENAI,
    TEST
}

object AIFactory {
    fun createAIModel(apiType: ApiType): AIModelDataSource{
        return when(apiType){
            ApiType.OPENAI -> OpenAIRemoteDataSource
            ApiType.TEST -> TestRemoteDataSource
        }
    }
}