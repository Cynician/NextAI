package com.android.nextai.domain.remote.entity


sealed interface ApiResult<out T> {

    data class Success<T>(
        val data: T? = null,
    ) : ApiResult<T>

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : ApiResult<Nothing>
}