package com.example.activitytrackerapp.data.database

import android.content.Context
import androidx.room.*
import com.example.activitytrackerapp.data.model.Achievement
import com.example.activitytrackerapp.data.model.Activity
import com.example.activitytrackerapp.data.model.User
import com.example.activitytrackerapp.data.model.Converters

@Database(
    entities = [Activity::class, User::class, Achievement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ActivityTrackerDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: ActivityTrackerDatabase? = null

        fun getDatabase(context: Context): ActivityTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ActivityTrackerDatabase::class.java,
                    "activity_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
