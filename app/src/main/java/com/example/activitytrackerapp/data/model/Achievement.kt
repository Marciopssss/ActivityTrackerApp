package com.example.activitytrackerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

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