package com.community.activitytrackerapp.data.repository

import com.community.activitytracker.data.database.ActivityDao
import com.community.activitytracker.data.database.UserDao
import com.community.activitytracker.data.database.AchievementDao
import com.community.activitytracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date

/**
 * Repository for Activity operations
 */
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

        // Update user stats
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
        val totalDistance = activities.sumOf { it.distance }
        val totalActivities = activities.size
        val totalDuration = activities.sumOf { it.duration }
        val avgSpeed = if (activities.isNotEmpty()) {
            activities.map { it.avgSpeed }.average()
        } else 0.0
        val totalCalories = activities.sumOf { it.calories }

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

/**
 * Repository for User operations
 */
class UserRepository(
    private val userDao: UserDao
) {
    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdFlow(userId: String): Flow<User?> {
        return userDao.getUserByIdFlow(userId)
    }

    fun getLeaderboard(limit: Int = 10): Flow<List<LeaderboardEntry>> {
        return userDao.getTopUsersByDistance(limit).map { users ->
            users.mapIndexed { index, user ->
                LeaderboardEntry(
                    userId = user.id,
                    username = user.username,
                    totalDistance = user.totalDistance,
                    totalActivities = user.totalActivities,
                    rank = index + 1,
                    profileImageUrl = user.profileImageUrl
                )
            }
        }
    }

    suspend fun updateStreak(userId: String, streak: Int) {
        userDao.updateStreak(userId, streak)
    }
}

/**
 * Repository for Achievement operations
 */
class AchievementRepository(
    private val achievementDao: AchievementDao
) {
    fun getAchievementsByUser(userId: String): Flow<List<Achievement>> {
        return achievementDao.getAchievementsByUser(userId)
    }

    fun getUnlockedAchievements(userId: String): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievements(userId)
    }

    suspend fun insertAchievement(achievement: Achievement): Long {
        return achievementDao.insert(achievement)
    }

    suspend fun unlockAchievement(achievementId: Long) {
        achievementDao.unlockAchievement(achievementId, Date())
    }

    suspend fun initializeDefaultAchievements(userId: String) {
        val achievements = listOf(
            Achievement(userId = userId, title = "First Steps", description = "Complete your first activity", type = AchievementType.ACTIVITIES, threshold = 1),
            Achievement(userId = userId, title = "10K Club", description = "Walk/Run 10 kilometers", type = AchievementType.DISTANCE, threshold = 10000),
            Achievement(userId = userId, title = "Marathon", description = "Complete 42.2 kilometers", type = AchievementType.DISTANCE, threshold = 42195),
            Achievement(userId = userId, title = "Consistent", description = "Maintain a 7-day streak", type = AchievementType.STREAK, threshold = 7),
            Achievement(userId = userId, title = "Dedicated", description = "Complete 50 activities", type = AchievementType.ACTIVITIES, threshold = 50),
            Achievement(userId = userId, title = "Speed Demon", description = "Reach 25 km/h", type = AchievementType.SPEED, threshold = 25)
        )

        achievements.forEach { insertAchievement(it) }
    }
}