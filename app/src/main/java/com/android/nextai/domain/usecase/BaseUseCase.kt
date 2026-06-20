package com.android.nextai.domain.usecase


abstract class BaseUseCase {
    suspend fun <R> execute(block: suspend () -> R): Result<R> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}