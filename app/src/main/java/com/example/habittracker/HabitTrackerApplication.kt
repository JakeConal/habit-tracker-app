package com.example.habittracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Custom Application class for Habit Tracker.
 *
 * This class is the entry point for the application and is used for:
 * - Initializing Hilt dependency injection
 * - Setting up global configurations
 * - Initializing third-party libraries
 *
 * To use this class, add it to AndroidManifest.xml:
 * <application
 *     android:name=".HabitTrackerApplication"
 *     ...>
 *
 * When Hilt is enabled, add @HiltAndroidApp annotation:
 * @HiltAndroidApp
 * class HabitTrackerApplication : Application() { ... }
 */
class HabitTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize instance
        instance = this

        FirebaseApp.initializeApp(this)
        setupFirestoreCaching()
        createNotificationChannel()

        // Initialize application-wide configurations
        initializeApp()
    }

    private fun setupFirestoreCaching() {
        // Enable offline persistence for Firestore using the modern API
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build())
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "habit_tracker_default_channel"
            val channelName = "Habit Tracker Notifications"
            val descriptionText = "Notifications for likes, comments, and shares"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeApp() {
        // Hide system navigation bar globally for immersive experience
        setUpNavigationBarHiding()

        // TODO: Initialize Timber for logging
        // Timber.plant(Timber.DebugTree())
        
        // TODO: Initialize other third-party libraries
        // - Firebase
        // - Analytics
        // - Crash reporting
    }

    /**
     * Configure the application to hide the system navigation bar (3-button navigation)
     * This creates a more immersive full-screen experience
     */
    private fun setUpNavigationBarHiding() {
        // This flag will be used to hide navigation bar in activities
        // Activities should implement View.OnSystemUiVisibilityChangeListener to handle nav bar hiding
    }

    companion object {
        private var instance: HabitTrackerApplication? = null

        fun getInstance(): HabitTrackerApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}
