package com.voicetasker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for VoiceTasker.
 *
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application. It also implements WorkManager's Configuration.Provider
 * for custom WorkManager configuration with Hilt integration.
 */
@HiltAndroidApp
class VoiceTaskerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.DEBUG
                else android.util.Log.INFO
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Creates notification channels required by the app.
     * Required for Android 8.0 (API 26) and above.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Reminders channel - high importance for time-sensitive reminders
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task reminders and alerts"
                enableVibration(true)
                enableLights(true)
            }

            // Tasks channel - default importance for task updates
            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Tasks",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Task creation and status updates"
            }

            // Sync channel - low importance for background sync
            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background synchronization status"
            }

            // Voice channel - for voice recording service
            val voiceChannel = NotificationChannel(
                CHANNEL_VOICE,
                "Voice Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Voice recording service notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(remindersChannel, tasksChannel, syncChannel, voiceChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "voicetasker_reminders"
        const val CHANNEL_TASKS = "voicetasker_tasks"
        const val CHANNEL_SYNC = "voicetasker_sync"
        const val CHANNEL_VOICE = "voicetasker_voice"
    }
}
