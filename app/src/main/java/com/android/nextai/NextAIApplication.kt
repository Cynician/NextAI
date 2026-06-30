package com.android.nextai

import android.app.Application
import android.util.Log
import com.android.nextai.service.GenerationForegroundService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class NextAIApplication: Application() {

    companion object {
        private const val TAG = "NextAIApplication"
    }

    /**
     * Application-scoped coroutine scope that survives ViewModel lifecycle.
     * Used for long-running background tasks like AI streaming generation
     * that must continue even when the app goes to the background.
     */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        // Create notification channel for foreground service
        GenerationForegroundService.createNotificationChannel(this)
    }
}