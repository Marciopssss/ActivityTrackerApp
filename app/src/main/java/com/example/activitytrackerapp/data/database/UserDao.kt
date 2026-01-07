package com.example.activitytrackerapp.data.database

import androidx.room.*
import com.example.activitytrackerapp.data.model.User
import kotlinx.coroutines.flow.Flow

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

    // ✅ Add this to fix BackupManager
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}
