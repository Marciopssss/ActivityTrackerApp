package com.example.activitytrackerapp.data.database

import androidx.room.*
import com.example.activitytrackerapp.data.model.Achievement
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement): Long

    @Update
    suspend fun update(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievementsByUser(userId: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 1")
    fun getUnlockedAchievements(userId: String): Flow<List<Achievement>>

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :date WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: Long, date: Date)
}
