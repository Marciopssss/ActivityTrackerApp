package com.example.activitytrackerapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.activitytrackerapp.data.model.*
import com.example.activitytrackerapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
