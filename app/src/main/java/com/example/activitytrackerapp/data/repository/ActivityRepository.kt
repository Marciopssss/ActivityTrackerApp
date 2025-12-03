package com.example.activitytrackerapp.data.repository

import com.example.activitytrackerapp.data.database.ActivityDao
import com.example.activitytrackerapp.data.database.UserDao
import com.example.activitytrackerapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val userDao: UserDao
) {

    fun getActivitiesByUser(userId: String): Flow<List<Activity>> {
        return activityDao.getActivitiesByUser(userId)
    }

    fun getRecentActivities(userId: String, limit: Int = 10): Flow<List<Activity>> {
        return activityDao.getRecentActivities(userId, limit)
    }

    suspend fun insertActivity(activity: Activity): Long {
        return activityDao.insert(activity)
    }

    suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity)
    }

    suspend fun completeActivity(activity: Activity) {
        val completed = activity.copy(
            endTime = Date(),
            isCompleted = true
        )
        activityDao.update(completed)

        userDao.updateUserStats(activity.userId, activity.distance)
    }

    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
    }

    suspend fun getActivityById(activityId: Long): Activity? {
        return activityDao.getActivityById(activityId)
    }

    fun getActivitiesByType(userId: String, type: ActivityType): Flow<List<Activity>> {
        return activityDao.getActivitiesByType(userId, type)
    }

    suspend fun getTotalDistance(userId: String): Double {
        return activityDao.getTotalDistance(userId) ?: 0.0
    }

    suspend fun getTotalActivities(userId: String): Int {
        return activityDao.getTotalActivities(userId)
    }

    fun getWeeklyStats(userId: String): Flow<PeriodStats> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val weekStart = calendar.time

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = calendar.time

        return activityDao.getActivitiesInPeriod(userId, weekStart, weekEnd).map { activities ->
            calculatePeriodStats(activities, "Week ${calendar.get(Calendar.WEEK_OF_YEAR)}")
        }
    }

    fun getMonthlyStats(userId: String): Flow<PeriodStats> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.time

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.time

        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)

        return activityDao.getActivitiesInPeriod(userId, monthStart, monthEnd).map { activities ->
            calculatePeriodStats(activities, "$monthName $year")
        }
    }

    private fun calculatePeriodStats(activities: List<Activity>, period: String): PeriodStats {
        val totalDistance = activities.sumOf { it.distance.toDouble() }
        val totalActivities = activities.size
        val totalDuration = activities.sumOf { it.duration.toLong() }
        val avgSpeed = if (activities.isNotEmpty()) {
            activities.map { it.avgSpeed }.average()
        } else 0.0
        val totalCalories = activities.sumOf { it.calories.toInt() }

        return PeriodStats(
            period = period,
            totalDistance = totalDistance,
            totalActivities = totalActivities,
            totalDuration = totalDuration,
            avgSpeed = avgSpeed,
            totalCalories = totalCalories
        )
    }
}
