package com.community.activitytracker.data.database

import android.content.Context
import androidx.room.*
import com.community.activitytracker.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Activity DAO
 */
@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: Activity): Long

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): Activity?

    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY startTime DESC")
    fun getActivitiesByUser(userId: String): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE userId = :userId AND isCompleted = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getRecentActivities(userId: String, limit: Int = 10): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE userId = :userId AND startTime >= :startDate AND startTime <= :endDate")
    fun getActivitiesInPeriod(userId: String, startDate: Date, endDate: Date): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE userId = :userId AND type = :type")
    fun getActivitiesByType(userId: String, type: ActivityType): Flow<List<Activity>>

    @Query("SELECT SUM(distance) FROM activities WHERE userId = :userId AND isCompleted = 1")
    suspend fun getTotalDistance(userId: String): Double?

    @Query("SELECT COUNT(*) FROM activities WHERE userId = :userId AND isCompleted = 1")
    suspend fun getTotalActivities(userId: String): Int

    @Query("SELECT * FROM activities ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestActivity(): Activity?
}

/**
 * User DAO
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users ORDER BY totalDistance DESC LIMIT :limit")
    fun getTopUsersByDistance(limit: Int = 10): Flow<List<User>>

    @Query("UPDATE users SET totalDistance = totalDistance + :distance, totalActivities = totalActivities + 1 WHERE id = :userId")
    suspend fun updateUserStats(userId: String, distance: Double)

    @Query("UPDATE users SET currentStreak = :streak WHERE id = :userId")
    suspend fun updateStreak(userId: String, streak: Int)
}

/**
 * Achievement DAO
 */
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

/**
 * Room Database
 */
@Database(
    entities = [Activity::class, User::class, Achievement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ActivityTrackerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: ActivityTrackerDatabase? = null

        fun getDatabase(context: Context): ActivityTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ActivityTrackerDatabase::class.java,
                    "activity_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}