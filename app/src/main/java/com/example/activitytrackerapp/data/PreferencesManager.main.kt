package com.example.activitytrackerapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages app preferences using DataStore
 */
class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val WEIGHT = doublePreferencesKey("weight")
        val HEIGHT = doublePreferencesKey("height")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system") // metric or imperial
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_TIME = intPreferencesKey("reminder_time") // hour of day
        val THEME_MODE = stringPreferencesKey("theme_mode") // light, dark, auto
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    // User ID
    val userId: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID] ?: "user_default"
        }

    suspend fun setUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    // Username
    val username: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME] ?: "Guest User"
        }

    suspend fun setUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    // Email
    val userEmail: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] ?: ""
        }

    suspend fun setUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    // Weight (in kg)
    val weight: Flow<Double> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.WEIGHT] ?: 70.0
        }

    suspend fun setWeight(weight: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT] = weight
        }
    }

    // Height (in cm)
    val height: Flow<Double> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.HEIGHT] ?: 170.0
        }

    suspend fun setHeight(height: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEIGHT] = height
        }
    }

    // Unit System
    val unitSystem: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.UNIT_SYSTEM] ?: "metric"
        }

    suspend fun setUnitSystem(system: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNIT_SYSTEM] = system
        }
    }

    // Notifications Enabled
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Reminder Time
    val reminderTime: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] ?: 18 // 6 PM default
        }

    suspend fun setReminderTime(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] = hour
        }
    }

    // Theme Mode
    val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "auto"
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    // First Launch
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
        }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = isFirst
        }
    }

    // Clear all preferences
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}