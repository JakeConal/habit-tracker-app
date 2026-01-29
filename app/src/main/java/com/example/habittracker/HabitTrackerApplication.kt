package com.example.habittracker

import android.app.Application
import com.example.habittracker.util.NotificationHelper
import com.example.habittracker.util.ReminderScheduler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Custom Application class for Habit Tracker.
 * Manages global initializations and configurations.
 */
class HabitTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        setupFirestoreCaching()

        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule reminders based on user preferences
        ReminderScheduler.scheduleFromPreferences(this)
    }

    private fun setupFirestoreCaching() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Settings can only be set before any other firestore operations
            android.util.Log.w("HabitTrackerApp", "Firestore settings could not be set: ${e.message}")
        }
    }
}
