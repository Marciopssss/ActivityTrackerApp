package com.example.activitytrackerapp.worker

import android.content.Context
import androidx.work.*
import com.example.activitytrackerapp.data.database.ActivityTrackerDatabase
import com.example.activitytrackerapp.data.repository.*
import java.util.concurrent.TimeUnit

/**
 * Worker for syncing data and sending periodic reminders
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = ActivityTrackerDatabase.getDatabase(applicationContext)
            val userRepository = UserRepository(database.userDao())

            // Perform sync operations
            // This is where you'd sync with a backend server
            // For now, we'll just perform local database maintenance

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "activity_sync_work"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}

/**
 * Worker for sending activity reminders
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Check if user has been active today
            // Send notification if not
            sendReminderNotification()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendReminderNotification() {
        // Notification implementation
        // This would use NotificationManager to send a reminder
    }

    companion object {
        const val WORK_NAME = "activity_reminder_work"

        fun scheduleReminder(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(8, TimeUnit.HOURS) // Send at a specific time
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
            )
        }
    }
}