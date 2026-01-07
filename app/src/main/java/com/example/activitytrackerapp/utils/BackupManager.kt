package com.example.activitytrackerapp.utils

import android.content.Context
import android.net.Uri
import com.example.activitytrackerapp.data.database.ActivityTrackerDatabase
import com.example.activitytrackerapp.data.model.Activity
import com.example.activitytrackerapp.data.model.User
import com.example.activitytrackerapp.data.model.Achievement
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class BackupData(
    val activities: List<Activity>,
    val users: List<User>,
    val achievements: List<Achievement>,
    val backupDate: String,
    val version: String = "1.0"
)

object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackup(
        context: Context,
        database: ActivityTrackerDatabase
    ): File = withContext(Dispatchers.IO) {

        val activityDao = database.activityDao()
        val userDao = database.userDao()
        val achievementDao = database.achievementDao()

        val activities = activityDao.getAllActivitiesOnce() // Fetch all activities
        val users = userDao.getAllUsers() // Fetch all users
        val achievements = achievementDao.getAllAchievements() // Fetch all achievements

        val backupData = BackupData(
            activities = activities,
            users = users,
            achievements = achievements,
            backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            version = "1.0"
        )

        val backupDir = File(context.getExternalFilesDir(null), "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupDir, "activity_tracker_backup_$timestamp.json")

        backupFile.writeText(gson.toJson(backupData))
        backupFile
    }

    suspend fun restoreBackup(
        context: Context,
        uri: Uri,
        database: ActivityTrackerDatabase
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val backupData = gson.fromJson(jsonString, BackupData::class.java)

            val activityDao = database.activityDao()
            val userDao = database.userDao()
            val achievementDao = database.achievementDao()

            // Clear current data first (optional, depends on your app)
            // activityDao.deleteAll()
            // userDao.deleteAll()
            // achievementDao.deleteAll()

            backupData.users.forEach { userDao.insert(it) }
            backupData.activities.forEach { activityDao.insert(it) }
            backupData.achievements.forEach { achievementDao.insert(it) }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareBackup(context: Context, backupFile: File): Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
    }

    fun getAvailableBackups(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun deleteBackup(file: File): Boolean = file.delete()
}
