package com.example.activitytrackerapp.ui.screens

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.activitytrackerapp.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.activitytrackerapp.data.model.Activity
import com.example.activitytrackerapp.data.model.ActivityType
import com.example.activitytrackerapp.service.TrackingService
import com.example.activitytrackerapp.viewModel.TrackingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TrackingViewModel,
    trackingService: TrackingService?,
    onNavigateToTracking: () -> Unit
) {
    val recentActivities by viewModel.recentActivities.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToTracking,
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Start") },
                text = { Text("Start Activity") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Cards
            item {
                Text(
                    "This Week",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Distance",
                        value = "%.2f km".format(weeklyStats.totalDistance / 1000),
                        icon = Icons.Default.LocationOn,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Activities",
                        value = weeklyStats.totalActivities.toString(),
                        icon = Icons.Default.DirectionsRun,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Avg Speed",
                        value = "%.1f km/h".format(weeklyStats.avgSpeed * 3.6),
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Calories",
                        value = weeklyStats.totalCalories.toString(),
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Activities
            item {
                Text(
                    "Recent Activities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (recentActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No activities yet. Start tracking!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(recentActivities) { activity ->
                    ActivityCard(activity = activity)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ActivityCard(activity: Activity) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = when(activity.type) {
                    ActivityType.WALKING -> painterResource(id = R.drawable.ic_walking)
                    ActivityType.JOGGING -> painterResource(id = R.drawable.ic_jogging)
                    ActivityType.RUNNING -> painterResource(id = R.drawable.ic_running)
                    ActivityType.CYCLING -> painterResource(id = R.drawable.ic_cycling)
                    ActivityType.SPEED -> painterResource(id = R.drawable.ic_speed)
                    ActivityType.ACTIVITIES -> painterResource(id = R.drawable.ic_activities)
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.type.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    dateFormat.format(activity.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "%.2f km".format(activity.distance / 1000),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${activity.duration / 60000} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "%.1f km/h".format(activity.avgSpeed * 3.6),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("leaderboard", "Leaderboard", Icons.Default.Leaderboard),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)