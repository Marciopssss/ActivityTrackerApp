package com.example.activitytrackerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "activities")
@TypeConverters(Converters::class)
data class `Activity.kt`(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val type: ActivityType,
    val startTime: Date,
    val endTime: Date?,
    val duration: Long,
    val distance: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val calories: Int,
    val routePoints: List<LocationPoint>,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
)