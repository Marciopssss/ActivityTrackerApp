package com.example.activitytrackerapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.community.activitytracker.data.database.ActivityTrackerDatabase
import com.community.activitytracker.data.repository.ActivityRepository
import com.community.activitytracker.data.repository.AchievementRepository
import com.community.activitytracker.data.repository.UserRepository
import com.community.activitytracker.service.TrackingService
import com.community.activitytracker.ui.screens.*
import com.community.activitytracker.ui.theme.ActivityTrackerTheme
import com.community.activitytracker.viewmodel.LeaderboardViewModel
import com.community.activitytracker.viewmodel.ProfileViewModel
import com.community.activitytracker.viewmodel.TrackingViewModel

class MainActivity : ComponentActivity() {

    private var trackingService: TrackingService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.TrackingBinder
            trackingService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            serviceBound = false
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Fine location granted
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Coarse location granted
            }
            else -> {
                // No location access granted
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repositories
        val database = ActivityTrackerDatabase.getDatabase(applicationContext)
        val activityRepository = ActivityRepository(database.activityDao(), database.userDao())
        val userRepository = UserRepository(database.userDao())
        val achievementRepository = AchievementRepository(database.achievementDao())

        // Request permissions
        checkAndRequestPermissions()

        setContent {
            ActivityTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityTrackerApp(
                        activityRepository = activityRepository,
                        userRepository = userRepository,
                        achievementRepository = achievementRepository,
                        trackingService = trackingService
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, TrackingService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            locationPermissionRequest.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Composable
fun ActivityTrackerApp(
    activityRepository: ActivityRepository,
    userRepository: UserRepository,
    achievementRepository: AchievementRepository,
    trackingService: TrackingService?
) {
    val navController = rememberNavController()

    // Create ViewModels
    val trackingViewModel: TrackingViewModel = viewModel(
        factory = TrackingViewModelFactory(activityRepository, userRepository)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository, activityRepository, achievementRepository)
    )
    val leaderboardViewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModelFactory(userRepository)
    )

    // Initialize user on first launch
    LaunchedEffect(Unit) {
        profileViewModel.initializeUser()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = trackingViewModel,
                    trackingService = trackingService,
                    onNavigateToTracking = { navController.navigate("tracking") }
                )
            }
            composable("tracking") {
                TrackingScreen(
                    viewModel = trackingViewModel,
                    trackingService = trackingService,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("leaderboard") {
                LeaderboardScreen(viewModel = leaderboardViewModel)
            }
            composable("profile") {
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}

// ViewModel Factories
class TrackingViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackingViewModel(activityRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository,
    private val achievementRepository: AchievementRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository, activityRepository, achievementRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LeaderboardViewModelFactory(
    private val userRepository: UserRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeaderboardViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}