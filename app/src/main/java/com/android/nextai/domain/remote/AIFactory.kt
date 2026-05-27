package com.android.nextai.domain.remote

import com.android.nextai.domain.remote.qianwen.QianwenRemoteDataSource
import com.android.nextai.domain.remote.test.TestRemoteDataSource

enum class Model{
    QIANWEN,
    TEST
}

object AIFactory {
    fun createAIModel(model: Model): AIModelDataSource{
        return when(model){
            Model.QIANWEN -> QianwenRemoteDataSource
            Model.TEST -> TestRemoteDataSource
        }
    }
}