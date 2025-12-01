package com.example.activitytrackerapp.theme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ActivityTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Tracking notification channel
            val trackingChannel = NotificationChannel(
                TRACKING_CHANNEL_ID,
                "Activity Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing activity tracking"
                setShowBadge(false)
            }

            // Reminder notification channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Activity Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to stay active"
                enableVibration(true)
            }

            // Achievement notification channel
            val achievementChannel = NotificationChannel(
                ACHIEVEMENT_CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when you unlock achievements"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(achievementChannel)
        }
    }

    companion object {
        const val TRACKING_CHANNEL_ID = "activity_tracking_channel"
        const val REMINDER_CHANNEL_ID = "activity_reminder_channel"
        const val ACHIEVEMENT_CHANNEL_ID = "achievement_channel"
    }
}