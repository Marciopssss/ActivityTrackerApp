package com.example.activitytrackerapp.data.repository

import com.example.activitytrackerapp.data.database.AchievementDao
import com.example.activitytrackerapp.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

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
