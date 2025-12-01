package com.example.activitytrackerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Activity Types
 */
enum class ActivityType {
    WALKING,
    JOGGING,
    CYCLING
}

/**
 * Location Point for route tracking
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val altitude: Double = 0.0
)

/**
 * Activity Entity - Represents a single tracking session
 */
@Entity(tableName = "activities")
@TypeConverters(Converters::class)
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val type: ActivityType,
    val startTime: Date,
    val endTime: Date?,
    val duration: Long, // in milliseconds
    val distance: Double, // in meters
    val avgSpeed: Double, // in m/s
    val maxSpeed: Double, // in m/s
    val calories: Int,
    val routePoints: List<LocationPoint>,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
)

/**
 * User Entity
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val totalDistance: Double = 0.0, // in meters
    val totalActivities: Int = 0,
    val totalCalories: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val joinDate: Date = Date()
)

/**
 * Achievement Entity
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val threshold: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Date? = null
)

enum class AchievementType {
    DISTANCE,
    ACTIVITIES,
    STREAK,
    SPEED
}

/**
 * Leaderboard Entry
 */
data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val totalDistance: Double,
    val totalActivities: Int,
    val rank: Int,
    val profileImageUrl: String? = null
)

/**
 * Weekly/Monthly Statistics
 */
data class PeriodStats(
    val period: String, // e.g., "Week 48" or "November 2024"
    val totalDistance: Double,
    val totalActivities: Int,
    val totalDuration: Long,
    val avgSpeed: Double,
    val totalCalories: Int
)

/**
 * Type Converters for Room Database
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromActivityType(value: ActivityType): String {
        return value.name
    }

    @TypeConverter
    fun toActivityType(value: String): ActivityType {
        return ActivityType.valueOf(value)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun fromLocationPointList(points: List<LocationPoint>): String {
        return gson.toJson(points)
    }

    @TypeConverter
    fun toLocationPointList(value: String): List<LocationPoint> {
        val listType = object : TypeToken<List<LocationPoint>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromAchievementType(value: AchievementType): String {
        return value.name
    }

    @TypeConverter
    fun toAchievementType(value: String): AchievementType {
        return AchievementType.valueOf(value)
    }
}