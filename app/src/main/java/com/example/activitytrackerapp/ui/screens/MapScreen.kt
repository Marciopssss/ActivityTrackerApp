package com.example.activitytrackerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.activitytrackerapp.data.model.Activity
import com.example.activitytrackerapp.data.model.LocationPoint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    activity: Activity?,
    onNavigateBack: () -> Unit
) {
    val userPins = remember { mutableStateListOf<LatLng>() }
    val cameraPositionState = rememberCameraPositionState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Map") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            if (activity != null && activity.routePoints.isNotEmpty()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(mapType = MapType.NORMAL),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        compassEnabled = true
                    ),
                    onMapClick = { latLng -> userPins.add(latLng) }
                ) {
                    // Draw route
                    Polyline(
                        points = activity.routePoints.map { LatLng(it.latitude, it.longitude) },
                        color = Color.Blue,
                        width = 12f
                    )

                    // Start marker
                    activity.routePoints.firstOrNull()?.let { start ->
                        Marker(
                            state = MarkerState(LatLng(start.latitude, start.longitude)),
                            title = "Start"
                        )
                    }

                    // End marker
                    activity.routePoints.lastOrNull()?.let { end ->
                        Marker(
                            state = MarkerState(LatLng(end.latitude, end.longitude)),
                            title = "End"
                        )
                    }

                    // User-added pins
                    userPins.forEach { latLng ->
                        Marker(
                            state = MarkerState(latLng),
                            title = "Pinned Location"
                        )
                    }
                }

                // Auto-zoom to route
                LaunchedEffect(activity.routePoints) {
                    val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                    activity.routePoints.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
                    )
                }

            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No route data available", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
