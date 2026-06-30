package com.android.nextai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.nextai.R

/**
 * A lightweight foreground service that keeps the process alive
 * during AI streaming generation so the network connection isn't
 * interrupted when the app goes to the background.
 */
class GenerationForegroundService : Service() {

    companion object {
        private const val TAG = "GenForegroundService"
        const val CHANNEL_ID = "generation_channel"
        const val CHANNEL_NAME = "AI生成任务"
        const val NOTIFICATION_ID = 1001

        private const val EXTRA_IS_ACTIVE = "is_active"

        /**
         * Start the foreground service to indicate a generation task is active.
         */
        fun start(context: Context) {
            val intent = Intent(context, GenerationForegroundService::class.java).apply {
                putExtra(EXTRA_IS_ACTIVE, true)
            }
            context.startForegroundService(intent)
        }

        /**
         * Stop the foreground service when the generation task completes.
         */
        fun stop(context: Context) {
            val intent = Intent(context, GenerationForegroundService::class.java)
            context.stopService(intent)
        }

        /**
         * Create the notification channel (call once, e.g. in Application.onCreate).
         */
        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于在后台持续进行AI生成任务"
                setShowBadge(false)
            }
            val manager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isActive = intent?.getBooleanExtra(EXTRA_IS_ACTIVE, false) ?: false
        Log.d(TAG, "onStartCommand isActive=$isActive")

        if (isActive) {
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI 正在生成回复...")
            .setContentText("运行中...")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }
}
