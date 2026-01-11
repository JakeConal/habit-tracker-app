package com.example.habittracker

import android.app.Application
import android.view.View

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

        // Initialize application-wide configurations
        initializeApp()
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

