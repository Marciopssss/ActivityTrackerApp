package com.example.activitytrackerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val totalDistance: Double = 0.0,
    val totalActivities: Int = 0,
    val totalCalories: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val joinDate: Date = Date()
)