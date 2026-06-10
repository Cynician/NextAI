package com.android.nextai.domain.model

enum class SessionGroup(val title:String){
    PIN("置顶"),
    TODAY("今天"),
    IN_WEEK("最近7天"),
    IN_MONTH("最近30天"),
    EARLIER("更早")
}