package com.example.activitytrackerapp.data.repository

import com.example.activitytrackerapp.data.database.UserDao
import com.example.activitytrackerapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdFlow(userId: String): Flow<User?> {
        return userDao.getUserByIdFlow(userId)
    }

    fun getLeaderboard(limit: Int = 10): Flow<List<LeaderboardEntry>> {
        return userDao.getTopUsersByDistance(limit).map { users ->
            users.mapIndexed { index, user ->
                LeaderboardEntry(
                    userId = user.id,
                    username = user.username,
                    totalDistance = user.totalDistance,
                    totalActivities = user.totalActivities,
                    rank = index + 1,
                    profileImageUrl = user.profileImageUrl
                )
            }
        }
    }

    suspend fun updateStreak(userId: String, streak: Int) {
        userDao.updateStreak(userId, streak)
    }
}
