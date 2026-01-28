package com.example.habittracker.util

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.habittracker.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Utility object for scheduling and canceling daily reminder work using WorkManager.
 * 
 * The reminder is scheduled to run at a specific time each day.
 * WorkManager handles device restarts and ensures the work runs even if the app is closed.
 */
object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    /**
     * Schedule daily reminder at the specified time.
     * 
     * @param context Application context
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     */
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        Log.d(TAG, "Scheduling daily reminder for $hour:$minute")

        // Calculate initial delay until the scheduled time
        val initialDelay = calculateInitialDelay(hour, minute)

        // Create periodic work request that runs every 24 hours
        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS  // Repeat every 24 hours
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(ReminderWorker.WORK_NAME)
            .build()

        // Schedule the work, replacing any existing work with the same name
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // Replace existing work
            reminderWorkRequest
        )

        Log.d(TAG, "Daily reminder scheduled with initial delay: ${initialDelay / 1000 / 60} minutes")
    }

    /**
     * Cancel the daily reminder.
     * 
     * @param context Application context
     */
    fun cancelDailyReminder(context: Context) {
        Log.d(TAG, "Canceling daily reminder")
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }

    /**
     * Update the reminder time.
     * This cancels the existing work and schedules a new one with the updated time.
     * 
     * @param context Application context
     * @param hour New hour in 24-hour format
     * @param minute New minute
     */
    fun updateReminderTime(context: Context, hour: Int, minute: Int) {
        Log.d(TAG, "Updating reminder time to $hour:$minute")
        // Simply reschedule - CANCEL_AND_REENQUEUE policy will handle the replacement
        scheduleDailyReminder(context, hour, minute)
    }

    /**
     * Check if reminder is currently scheduled.
     * 
     * Note: This is a simple check based on preferences. WorkManager internally
     * tracks work state, but for UI purposes we rely on UserPreferences.
     * 
     * @param context Application context
     * @return true if reminder is enabled in preferences
     */
    fun isReminderScheduled(context: Context): Boolean {
        return UserPreferences.isReminderEnabled(context)
    }

    /**
     * Calculate the delay in milliseconds until the next occurrence of the specified time.
     * 
     * If the specified time has already passed today, the delay will be calculated
     * for the same time tomorrow.
     * 
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     * @return Delay in milliseconds
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val scheduled = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the scheduled time has already passed today, schedule for tomorrow
        if (scheduled.before(now) || scheduled == now) {
            scheduled.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delayMillis = scheduled.timeInMillis - now.timeInMillis
        Log.d(TAG, "Initial delay calculated: ${delayMillis}ms (${delayMillis / 1000 / 60} minutes)")
        
        return delayMillis
    }

    /**
     * Schedule reminder based on current preferences.
     * Call this on app startup to ensure reminder is scheduled if enabled.
     * 
     * @param context Application context
     */
    fun scheduleFromPreferences(context: Context) {
        if (UserPreferences.isReminderEnabled(context)) {
            val hour = UserPreferences.getReminderHour(context)
            val minute = UserPreferences.getReminderMinute(context)
            scheduleDailyReminder(context, hour, minute)
        }
    }
}
