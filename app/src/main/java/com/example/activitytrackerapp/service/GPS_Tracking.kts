package com.community.activitytracker.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.community.activitytracker.MainActivity
import com.community.activitytracker.R
import com.community.activitytracker.data.model.LocationPoint
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground Service for GPS tracking
 */
class TrackingService : Service() {

    private val binder = TrackingBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val routePoints: StateFlow<List<LocationPoint>> = _routePoints.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private var lastLocation: Location? = null
    private var startTime: Long = 0

    inner class TrackingBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.forEach { location ->
                    processNewLocation(location)
                }
            }
        }
    }

    fun startTracking() {
        if (_isTracking.value) return

        _isTracking.value = true
        _routePoints.value = emptyList()
        _totalDistance.value = 0.0
        startTime = System.currentTimeMillis()
        lastLocation = null

        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
    }

    fun stopTracking() {
        _isTracking.value = false
        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun pauseTracking() {
        _isTracking.value = false
        stopLocationUpdates()
    }

    fun resumeTracking() {
        _isTracking.value = true
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL)
            setWaitForAccurateLocation(true)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun processNewLocation(location: Location) {
        val locationPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = location.time,
            altitude = location.altitude
        )

        // Update route points
        _routePoints.value = _routePoints.value + locationPoint

        // Update speed
        _currentSpeed.value = location.speed

        // Calculate distance
        lastLocation?.let { last ->
            val distance = last.distanceTo(location).toDouble()
            if (distance > MIN_DISTANCE_THRESHOLD) {
                _totalDistance.value += distance
                lastLocation = location
            }
        } ?: run {
            lastLocation = location
        }

        // Update notification
        updateNotification()
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Activity Tracking")
            .setContentText("Tracking in progress...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val distance = _totalDistance.value / 1000.0 // Convert to kilometers
        val speed = _currentSpeed.value * 3.6 // Convert m/s to km/h

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Activity Tracking")
            .setContentText("Distance: %.2f km | Speed: %.1f km/h".format(distance, speed))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Activity Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing activity tracking"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getElapsedTime(): Long {
        return if (_isTracking.value && startTime > 0) {
            System.currentTimeMillis() - startTime
        } else 0L
    }

    fun getAverageSpeed(): Double {
        val elapsedTime = getElapsedTime() / 1000.0 // Convert to seconds
        return if (elapsedTime > 0) {
            _totalDistance.value / elapsedTime
        } else 0.0
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "activity_tracking_channel"
        private const val LOCATION_UPDATE_INTERVAL = 3000L // 3 seconds
        private const val FASTEST_LOCATION_UPDATE_INTERVAL = 1000L // 1 second
        private const val MIN_DISTANCE_THRESHOLD = 5.0 // 5 meters
    }
}