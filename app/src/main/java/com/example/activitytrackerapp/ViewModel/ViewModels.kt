package com.example.activitytrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.community.activitytracker.data.model.*
import com.community.activitytracker.data.repository.ActivityRepository
import com.community.activitytracker.data.repository.UserRepository
import com.community.activitytracker.data.repository.AchievementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Main ViewModel for tracking activities
 */
class TrackingViewModel(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow("user_default")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity.asStateFlow()

    val recentActivities = currentUserId.flatMapLatest { userId ->
        activityRepository.getRecentActivities(userId, 10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyStats = currentUserId.flatMapLatest { userId ->
        activityRepository.getWeeklyStats(userId)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PeriodStats("", 0.0, 0, 0, 0.0, 0)
    )

    val monthlyStats = currentUserId.flatMapLatest { userId ->
        activityRepository.getMonthlyStats(userId)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PeriodStats("", 0.0, 0, 0, 0.0, 0)
    )

    fun startActivity(type: ActivityType) {
        viewModelScope.launch {
            val activity = Activity(
                userId = _currentUserId.value,
                type = type,
                startTime = Date(),
                endTime = null,
                duration = 0,
                distance = 0.0,
                avgSpeed = 0.0,
                maxSpeed = 0.0,
                calories = 0,
                routePoints = emptyList(),
                isCompleted = false
            )
            val id = activityRepository.insertActivity(activity)
            _currentActivity.value = activity.copy(id = id)
            _trackingState.value = TrackingState.TRACKING
        }
    }

    fun updateActivity(
        distance: Double,
        speed: Double,
        routePoints: List<LocationPoint>,
        duration: Long
    ) {
        viewModelScope.launch {
            _currentActivity.value?.let { activity ->
                val maxSpeed = maxOf(activity.maxSpeed, speed)
                val avgSpeed = if (duration > 0) distance / (duration / 1000.0) else 0.0
                val calories = calculateCalories(activity.type, distance, duration)

                val updated = activity.copy(
                    distance = distance,
                    avgSpeed = avgSpeed,
                    maxSpeed = maxSpeed,
                    routePoints = routePoints,
                    duration = duration,
                    calories = calories
                )
                activityRepository.updateActivity(updated)
                _currentActivity.value = updated
            }
        }
    }

    fun pauseActivity() {
        _trackingState.value = TrackingState.PAUSED
    }

    fun resumeActivity() {
        _trackingState.value = TrackingState.TRACKING
    }

    fun completeActivity() {
        viewModelScope.launch {
            _currentActivity.value?.let { activity ->
                activityRepository.completeActivity(activity)
                _currentActivity.value = null
                _trackingState.value = TrackingState.IDLE
            }
        }
    }

    fun cancelActivity() {
        viewModelScope.launch {
            _currentActivity.value?.let { activity ->
                activityRepository.deleteActivity(activity)
                _currentActivity.value = null
                _trackingState.value = TrackingState.IDLE
            }
        }
    }

    private fun calculateCalories(type: ActivityType, distance: Double, duration: Long): Int {
        val hours = duration / (1000.0 * 60 * 60)
        val km = distance / 1000.0

        // MET values (Metabolic Equivalent of Task)
        val met = when (type) {
            ActivityType.WALKING -> 3.5
            ActivityType.JOGGING -> 7.0
            ActivityType.CYCLING -> 8.0
        }

        val weight = 70.0 // Assume 70kg, can be customized
        return (met * weight * hours).toInt()
    }

    enum class TrackingState {
        IDLE, TRACKING, PAUSED
    }
}

/**
 * ViewModel for user profile and statistics
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow("user_default")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val currentUser = currentUserId.flatMapLatest { userId ->
        userRepository.getUserByIdFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements = currentUserId.flatMapLatest { userId ->
        achievementRepository.getAchievementsByUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivities = currentUserId.flatMapLatest { userId ->
        activityRepository.getRecentActivities(userId, 20)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateUserProfile(username: String, email: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val updated = user.copy(username = username, email = email)
                userRepository.updateUser(updated)
            }
        }
    }

    fun initializeUser() {
        viewModelScope.launch {
            val existingUser = userRepository.getUserById(_currentUserId.value)
            if (existingUser == null) {
                val newUser = User(
                    id = _currentUserId.value,
                    username = "Guest User",
                    email = "guest@activitytracker.com"
                )
                userRepository.insertUser(newUser)
                achievementRepository.initializeDefaultAchievements(_currentUserId.value)
            }
        }
    }
}

/**
 * ViewModel for leaderboard
 */
class LeaderboardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val leaderboard = userRepository.getLeaderboard(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUserId = MutableStateFlow("user_default")

    val currentUserRank = combine(leaderboard, _currentUserId) { board, userId ->
        board.find { it.userId == userId }?.rank ?: -1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)
}