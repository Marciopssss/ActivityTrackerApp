package com.example.activitytrackerapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.activitytrackerapp.data.model.ActivityType
import com.example.activitytrackerapp.data.model.LocationPoint
import com.example.activitytrackerapp.service.TrackingService
import com.example.activitytrackerapp.viewModel.TrackingViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    trackingService: TrackingService?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val trackingState by viewModel.trackingState.collectAsState()

    var selectedActivityType by remember { mutableStateOf(ActivityType.WALKING) }
    var isTracking by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    val distance by trackingService?.totalDistance?.collectAsState() ?: remember { mutableStateOf(0.0) }
    val speed by trackingService?.currentSpeed?.collectAsState() ?: remember { mutableStateOf(0f) }
    val routePoints by trackingService?.routePoints?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    val elapsedTime by viewModel.elapsedTime.collectAsState()


    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions handled if needed */ }

    // Request permissions once
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Activity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                MapViewComponent(
                    routePoints = routePoints,
                    pins = listOfNotNull(routePoints.firstOrNull(), routePoints.lastOrNull())
                )
            }

            // Stats and Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Activity type selection if not tracking
                if (!isTracking) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Select Activity Type",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ActivityTypeButton(
                                type = ActivityType.WALKING,
                                icon = Icons.Default.DirectionsWalk,
                                isSelected = selectedActivityType == ActivityType.WALKING,
                                onClick = { selectedActivityType = ActivityType.WALKING },
                                modifier = Modifier.weight(1f)
                            )
                            ActivityTypeButton(
                                type = ActivityType.JOGGING,
                                icon = Icons.Default.DirectionsRun,
                                isSelected = selectedActivityType == ActivityType.JOGGING,
                                onClick = { selectedActivityType = ActivityType.JOGGING },
                                modifier = Modifier.weight(1f)
                            )
                            ActivityTypeButton(
                                type = ActivityType.CYCLING,
                                icon = Icons.Default.DirectionsBike,
                                isSelected = selectedActivityType == ActivityType.CYCLING,
                                onClick = { selectedActivityType = ActivityType.CYCLING },
                                modifier = Modifier.weight(1f)
                            )
                            ActivityTypeButton(
                                type = ActivityType.RUNNING,
                                icon = Icons.Default.DirectionsRun,
                                isSelected = selectedActivityType == ActivityType.RUNNING,
                                onClick = { selectedActivityType = ActivityType.RUNNING },
                                modifier = Modifier.weight(1f)
                            )

                        }
                    }
                }

                // Stats display if tracking
                if (isTracking) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            formatTime(elapsedTime),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip(
                                label = "Distance",
                                value = "%.2f km".format(distance / 1000),
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                label = "Speed",
                                value = "%.1f km/h".format(speed * 3.6),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Control Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isTracking) {
                        Button(
                            onClick = {
                                startTrackingService(context)
                                isTracking = true
                                viewModel.startActivity(selectedActivityType)
                                trackingService?.startTracking()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isPaused) {
                                Button(
                                    onClick = {
                                        isPaused = true
                                        trackingService?.pauseTracking()
                                        viewModel.pauseActivity()
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pause")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isPaused = false
                                        trackingService?.resumeTracking()
                                        viewModel.resumeActivity()
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume")
                                }
                            }

                            Button(
                                onClick = {
                                    trackingService?.routePoints?.value?.let { points ->
                                        viewModel.updateActivity(
                                            distance = distance,
                                            speed = speed.toDouble(),
                                            routePoints = points,
                                            duration = elapsedTime
                                        )
                                    }
                                    viewModel.completeActivity()
                                    trackingService?.stopTracking()
                                    isTracking = false
                                    isPaused = false
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Finish")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.cancelActivity()
                                trackingService?.stopTracking()
                                isTracking = false
                                isPaused = false
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MapViewComponent(
    routePoints: List<LocationPoint>,
    pins: List<LocationPoint> = emptyList(), // pre-defined pins if needed
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()
    val defaultLocation = LatLng(33.8886, 35.4955) // Beirut fallback

    // Mutable state for user-added pins
    var userPins by remember { mutableStateOf(listOf<LatLng>()) }

    // Combine route pins and user-added pins for display
    val allPins = userPins + pins.map { LatLng(it.latitude, it.longitude) }

    // Center camera on last route point if available
    LaunchedEffect(routePoints.lastOrNull()) {
        routePoints.lastOrNull()?.let { point ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(point.latitude, point.longitude),
                17f
            )
        } ?: run {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true,
            mapToolbarEnabled = false
        ),
        onMapClick = { latLng ->
            // Add pin on map tap
            userPins = userPins + latLng
        }
    ) {
        // Draw route polyline
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints.map { LatLng(it.latitude, it.longitude) },
                color = Color(0xFF2196F3),
                width = 12f
            )
        }

        // Start marker
        routePoints.firstOrNull()?.let { start ->
            Marker(
                state = MarkerState(LatLng(start.latitude, start.longitude)),
                title = "Start"
            )
        }

        // End marker
        routePoints.lastOrNull()?.let { end ->
            Marker(
                state = MarkerState(LatLng(end.latitude, end.longitude)),
                title = "End"
            )
        }

        // Show all user-added pins (or pre-defined pins)
        allPins.forEachIndexed { index, pin ->
            Marker(
                state = MarkerState(pin),
                title = "Pin ${index + 1}"
            )
        }
    }
}


@Composable
fun ActivityTypeButton(
    type: ActivityType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun startTrackingService(context: Context) {
    Intent(context, TrackingService::class.java).also {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(it)
        } else {
            context.startService(it)
        }
    }
}
