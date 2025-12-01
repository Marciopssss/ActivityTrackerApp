package com.community.activitytracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.community.activitytracker.MainActivity
import com.community.activitytracker.R

/**
 * Manages all notifications in the app
 */
object NotificationHelper {

    private const val CHANNEL_TRACKING = "tracking_channel"
    private const val CHANNEL_REMINDER = "reminder_channel"
    private const val CHANNEL_ACHIEVEMENT = "achievement_channel"

    private const val NOTIFICATION_TRACKING_ID = 1001
    private const val NOTIFICATION_REMINDER_ID = 1002
    private const val NOTIFICATION_ACHIEVEMENT_ID = 1003

    /**
     * Create notification channels
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Tracking channel
            val trackingChannel = NotificationChannel(
                CHANNEL_TRACKING,
                "Activity Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing activity tracking"
                setShowBadge(false)
            }

            // Reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Activity Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to stay active"
                enableVibration(true)
            }

            // Achievement channel
            val achievementChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENT,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when you unlock achievements"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(achievementChannel)
        }
    }

    /**
     * Create tracking notification
     */
    fun createTrackingNotification(
        context: Context,
        distance: Double,
        speed: Float,
        duration: Long
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val distanceKm = distance / 1000.0
        val speedKmh = speed * 3.6
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration / (1000 * 60)) % 60
        val seconds = (duration / 1000) % 60

        return NotificationCompat.Builder(context, CHANNEL_TRACKING)
            .setContentTitle("Activity Tracking")
            .setContentText("%.2f km | %.1f km/h | %02d:%02d:%02d".format(distanceKm, speedKmh, hours, minutes, seconds))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Send reminder notification
     */
    fun sendReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setContentTitle("Time to move!")
            .setContentText("You haven't tracked an activity today. Let's get moving!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_REMINDER_ID, notification)
    }

    /**
     * Send achievement unlocked notification
     */
    fun sendAchievementNotification(
        context: Context,
        achievementTitle: String,
        achievementDescription: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "profile")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
            .setContentTitle("Achievement Unlocked! 🏆")
            .setContentText("$achievementTitle: $achievementDescription")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ACHIEVEMENT_ID, notification)
    }

    /**
     * Cancel notification
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}