package com.example.activitytrackerapp.data.model

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val totalDistance: Double,
    val totalActivities: Int,
    val rank: Int,
    val profileImageUrl: String? = null
)


data class PeriodStats(
    val period: String,  // e.g. "Week 48", "November 2024"
    val totalDistance: Double,
    val totalActivities: Int,
    val totalDuration: Long,
    val avgSpeed: Double,
    val totalCalories: Int
)


