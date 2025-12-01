package com.example.activitytrackerapp.utils

import android.location.Location
import com.community.activitytracker.data.model.ActivityType
import com.community.activitytracker.data.model.LocationPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Location utilities for distance and speed calculations
 */
object LocationUtils {

    /**
     * Calculate total distance from list of location points
     */
    fun calculateTotalDistance(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += calculateDistance(points[i], points[i + 1])
        }
        return totalDistance
    }

    /**
     * Calculate distance between two location points (in meters)
     */
    fun calculateDistance(point1: LocationPoint, point2: LocationPoint): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            point1.latitude,
            point1.longitude,
            point2.latitude,
            point2.longitude,
            results
        )
        return results[0].toDouble()
    }

    /**
     * Calculate average speed (m/s)
     */
    fun calculateAverageSpeed(distance: Double, duration: Long): Double {
        if (duration == 0L) return 0.0
        val durationInSeconds = duration / 1000.0
        return distance / durationInSeconds
    }

    /**
     * Calculate instantaneous speed between two points (m/s)
     */
    fun calculateSpeed(point1: LocationPoint, point2: LocationPoint): Double {
        val distance = calculateDistance(point1, point2)
        val timeDiff = (point2.timestamp - point1.timestamp) / 1000.0 // seconds
        return if (timeDiff > 0) distance / timeDiff else 0.0
    }

    /**
     * Convert m/s to km/h
     */
    fun metersPerSecondToKilometersPerHour(speed: Double): Double {
        return speed * 3.6
    }

    /**
     * Convert meters to kilometers
     */
    fun metersToKilometers(meters: Double): Double {
        return meters / 1000.0
    }
}

/**
 * Time formatting utilities
 */
object TimeUtils {

    /**
     * Format duration in milliseconds to HH:MM:SS
     */
    fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    /**
     * Format duration to readable string
     */
    fun formatDurationReadable(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /**
     * Format date
     */
    fun formatDate(date: Date, pattern: String = "MMM dd, yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Format date and time
     */
    fun formatDateTime(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Get start of day
     */
    fun getStartOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Get end of day
     */
    fun getEndOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
}

/**
 * Calorie calculation utilities
 */
object CalorieCalculator {

    /**
     * Calculate calories burned based on activity type and data
     * MET (Metabolic Equivalent of Task) based calculation
     */
    fun calculateCalories(
        activityType: ActivityType,
        distance: Double, // in meters
        duration: Long, // in milliseconds
        weight: Double = 70.0 // in kg, default to 70kg
    ): Int {
        val hours = duration / (1000.0 * 60 * 60)
        val kilometers = distance / 1000.0

        // MET values
        val met = when (activityType) {
            ActivityType.WALKING -> {
                val speed = kilometers / hours
                when {
                    speed < 4.0 -> 3.0  // Slow walking
                    speed < 5.5 -> 3.5  // Normal walking
                    else -> 4.3         // Fast walking
                }
            }
            ActivityType.JOGGING -> {
                val speed = kilometers / hours
                when {
                    speed < 8.0 -> 7.0  // Light jogging
                    speed < 11.0 -> 8.3 // Moderate jogging
                    else -> 9.8         // Fast running
                }
            }
            ActivityType.CYCLING -> {
                val speed = kilometers / hours
                when {
                    speed < 16.0 -> 6.8  // Light cycling
                    speed < 20.0 -> 8.0  // Moderate cycling
                    speed < 25.0 -> 10.0 // Fast cycling
                    else -> 12.0         // Very fast cycling
                }
            }
        }

        // Calories = MET × weight (kg) × time (hours)
        return (met * weight * hours).toInt()
    }
}

/**
 * Stats calculation utilities
 */
object StatsCalculator {

    /**
     * Calculate pace (minutes per km)
     */
    fun calculatePace(distance: Double, duration: Long): Double {
        if (distance == 0.0) return 0.0
        val kilometers = distance / 1000.0
        val minutes = duration / (1000.0 * 60)
        return minutes / kilometers
    }

    /**
     * Format pace to string
     */
    fun formatPace(pace: Double): String {
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).toInt()
        return "%d:%02d min/km".format(minutes, seconds)
    }

    /**
     * Calculate elevation gain
     */
    fun calculateElevationGain(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0

        var gain = 0.0
        for (i in 1 until points.size) {
            val diff = points[i].altitude - points[i - 1].altitude
            if (diff > 0) {
                gain += diff
            }
        }
        return gain
    }
}

/**
 * Permission utilities
 */
object PermissionUtils {
    const val REQUEST_CODE_LOCATION = 1001
    const val REQUEST_CODE_BACKGROUND_LOCATION = 1002
    const val REQUEST_CODE_NOTIFICATION = 1003
}

/**
 * Validation utilities
 */
object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
}