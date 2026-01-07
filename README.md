# 🏃 Activity Tracker App

Activity Tracker App is an Android application designed to track physical activities such as walking, jogging, and cycling using GPS. It provides real-time workout statistics, route visualization, and activity history while following modern Android development best practices.

---

## 🎯 Features

### Core Features
- **GPS-based activity tracking** (walking, jogging, running, cycling)
- **Real-time metrics**:
  - Distance
  - Duration
  - Speed
  - Calories burned (MET-based calculation)
- **Route visualization** on Google Maps with live polyline drawing
- **Activity history** with detailed statistics
- **Offline data storage** using Room Database
- **Background tracking service** to continue tracking while the app is running
- **Automatic data backup** when the app moves to the background

### Progress & Statistics
- **Local leaderboard** based on recorded activities
- **User profile** with personal statistics
- **Achievements tracking** based on activity milestones

---

## 🛠️ Architecture

The app follows the **MVVM (Model–View–ViewModel)** architecture pattern:

- **UI Layer**: Jetpack Compose screens
- **ViewModel Layer**: Handles business logic and state management
- **Repository Layer**: Abstracts data access
- **Data Layer**: Room database for local persistence

This architecture ensures clean separation of concerns, lifecycle awareness, and maintainable code.

---

## 🧰 Technology Stack

- **Language**: Kotlin  
- **UI**: Jetpack Compose + Material Design 3  
- **Architecture**: MVVM  
- **Database**: Room  
- **Maps**: Google Maps SDK  
- **Location**: Fused Location Provider  
- **Async**: Coroutines, Flow, LiveData  
- **Dependency Injection**: Hilt / Dagger  

---

## 📋 Requirements

- **Android Studio**: Hedgehog (2023.1.1) or later  
- **Minimum SDK**: 24 (Android 7.0)  
- **Target SDK**: 34 (Android 14)  
- **Kotlin**: 1.9+  
- **Google Play Services**: Required for location services  

---

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Marciopssss/ActivityTrackerApp.git
cd ActivityTrackerApp

2. Configure Google Maps API
Create a Google Maps API key from Google Cloud Console
Enable Maps SDK for Android
Add the API key to local.properties:
MAPS_API_KEY=your_google_maps_api_key_here

3. Open & Run
Open the project in Android Studio

Sync Gradle files

Run the app on a physical device or emulator

📱 Permissions
The app requires the following permissions:

ACCESS_FINE_LOCATION – Accurate GPS tracking

ACCESS_COARSE_LOCATION – General location access

ACCESS_BACKGROUND_LOCATION – Tracking during background usage

FOREGROUND_SERVICE – Continuous tracking

POST_NOTIFICATIONS – Notifications (Android 13+)

🗂️ Project Structure

app/
├── data/
│   ├── local/              # Room database, DAO, entities
│   └── repository/         # Data access layer
├── domain/
│   └── model/              # Activity models & enums
├── service/
│   └── TrackingService.kt  # Background tracking service
├── ui/
│   ├── screens/            # Compose screens
│   ├── components/         # Reusable UI components
│   └── theme/              # Material 3 theming
├── utils/                  # Location & calculation helpers
└── viewmodel/
    └── TrackingViewModel.kt
🔄 How It Works
User selects an activity type and starts tracking

GPS updates are received at fixed intervals

Distance is calculated using the Haversine formula

Calories are calculated using MET values

UI updates automatically via LiveData / StateFlow

Route is drawn on Google Maps in real time

When tracking stops, the activity is saved to Room

⚠️ Challenges Encountered
Handling reliable data backup during app lifecycle changes

Solution:
Implemented an automatic backup mechanism triggered when the app moves to the background to prevent data loss.

🚀 Future Improvements
Cloud backup (Firebase)

Export activity history (CSV / GPX)

Dark mode improvements

Wear OS support

Social and sharing features

📝 Notes
GPS tracking works offline

Internet connection is required only for map tiles

All activity data is stored locally on the device

👨‍💻 Authors
Marc El Dahdah

Elie Hassoun

📌 License
This project was developed for educational purposes as part of an Android development course.

Happy Tracking! 🏃‍♂️🚴‍♀️
