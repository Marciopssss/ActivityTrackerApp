package com.example.activitytrackerapp.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    private val gson = Gson()

    // --- ActivityType ---
    @TypeConverter
    fun fromActivityType(value: ActivityType?): String? = value?.name

    @TypeConverter
    fun toActivityType(value: String?): ActivityType? = value?.let { ActivityType.valueOf(it) }

    // --- AchievementType ---
    @TypeConverter
    fun fromAchievementType(value: AchievementType?): String? = value?.name

    @TypeConverter
    fun toAchievementType(value: String?): AchievementType? = value?.let { AchievementType.valueOf(it) }

    // --- LocationPoint list ---
    @TypeConverter
    fun fromLocationPointList(list: List<LocationPoint>?): String? =
        list?.let { gson.toJson(it) }

    @TypeConverter
    fun toLocationPointList(json: String?): List<LocationPoint> =
        if (json.isNullOrEmpty()) emptyList()
        else gson.fromJson(json, object : TypeToken<List<LocationPoint>>() {}.type)

    // --- Date ---
    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(time: Long?): Date? = time?.let { Date(it) }
}
