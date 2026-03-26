package com.android.nextai.domain.remote

import com.android.nextai.domain.remote.doubao.DoubaoRemoteDataSource
import com.android.nextai.domain.remote.qianwen.QianwenRemoteDataSource

enum class Model{
    DOUBAO,
    QIANWEN
}

object AIFactory {
    fun createAIModel(model: Model): AIModelDataSource{
        return when(model){
            Model.DOUBAO -> DoubaoRemoteDataSource
            Model.QIANWEN -> QianwenRemoteDataSource
        }
    }
}