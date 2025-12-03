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
 * Location point for route tracking
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
    val duration: Long,       // milliseconds
    val distance: Double,     // meters
    val avgSpeed: Double,     // m/s
    val maxSpeed: Double,     // m/s
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
    val totalDistance: Double = 0.0,  // meters
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
 * Leaderboard entry (not a Room table)
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
 * Weekly / Monthly Statistics model
 */
data class PeriodStats(
    val period: String,  // e.g. "Week 48", "November 2024"
    val totalDistance: Double,
    val totalActivities: Int,
    val totalDuration: Long,
    val avgSpeed: Double,
    val totalCalories: Int
)

/**
 * Room Type Converters
 */
/**
 * Type Converters for Room Database
 */
class Converters {

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
        val sb = StringBuilder()
        points.forEachIndexed { index, point ->
            sb.append("${point.latitude},${point.longitude},${point.timestamp},${point.altitude}")
            if (index < points.size - 1) {
                sb.append(";")
            }
        }
        return sb.toString()
    }

    @TypeConverter
    fun toLocationPointList(value: String): List<LocationPoint> {
        if (value.isEmpty()) return emptyList()

        return value.split(";").mapNotNull { pointStr ->
            val parts = pointStr.split(",")
            if (parts.size >= 3) {
                LocationPoint(
                    latitude = parts[0].toDoubleOrNull() ?: 0.0,
                    longitude = parts[1].toDoubleOrNull() ?: 0.0,
                    timestamp = parts[2].toLongOrNull() ?: 0L,
                    altitude = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                )
            } else null
        }
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
