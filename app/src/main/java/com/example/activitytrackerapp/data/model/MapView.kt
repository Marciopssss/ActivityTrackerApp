package com.example.activitytrackerapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.activitytrackerapp.data.model.LocationPoint
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapView(
    routePoints: List<LocationPoint>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Update camera to follow latest location
    LaunchedEffect(routePoints.lastOrNull()) {
        routePoints.lastOrNull()?.let { point ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(point.latitude, point.longitude),
                16f
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    ) {
        // Draw route polyline
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints.map { LatLng(it.latitude, it.longitude) },
                color = Color(0xFF2196F3),
                width = 10f
            )
        }

        // Show start marker
        routePoints.firstOrNull()?.let { start ->
            Marker(
                state = MarkerState(position = LatLng(start.latitude, start.longitude)),
                title = "Start",
                snippet = "Starting point"
            )
        }

        // Show current location marker
        routePoints.lastOrNull()?.let { current ->
            Marker(
                state = MarkerState(position = LatLng(current.latitude, current.longitude)),
                title = "Current Location"
            )
        }
    }
}
