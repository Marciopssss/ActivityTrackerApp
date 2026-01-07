package com.example.activitytrackerapp.data.database

import androidx.room.*
import com.example.activitytrackerapp.data.model.Activity
import com.example.activitytrackerapp.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import java.util.Date

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

    @Query("SELECT * FROM activities")
    suspend fun getAllActivitiesOnce(): List<Activity>

}
