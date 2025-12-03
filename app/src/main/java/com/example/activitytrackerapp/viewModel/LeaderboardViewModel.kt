package com.example.activitytrackerapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.activitytrackerapp.data.repository.UserRepository
import kotlinx.coroutines.flow.*

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
