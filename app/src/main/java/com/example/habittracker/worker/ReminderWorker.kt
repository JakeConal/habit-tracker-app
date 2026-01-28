package com.example.habittracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.util.DateUtils
import com.example.habittracker.util.NotificationHelper
import com.example.habittracker.util.UserPreferences
import java.util.Calendar

/**
 * WorkManager Worker that checks for incomplete habits and sends reminder notification.
 * 
 * Logic:
 * 1. Get all habits for the current user
 * 2. Filter habits scheduled for today (based on frequency)
 * 3. Check which habits are not completed for today
 * 4. If there are incomplete habits, show notification
 * 5. If all habits are complete, do nothing
 */
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "ReminderWorker"
        const val WORK_NAME = "daily_habit_reminder"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "ReminderWorker started")

        return try {
            // Check if reminder is still enabled (user might have disabled it)
            if (!UserPreferences.isReminderEnabled(applicationContext)) {
                Log.d(TAG, "Reminder is disabled, skipping")
                return Result.success()
            }

            // Check if notifications are enabled globally
            if (!UserPreferences.areNotificationsEnabled(applicationContext)) {
                Log.d(TAG, "Notifications are disabled globally, skipping")
                return Result.success()
            }

            // Get current user ID
            val userId = UserPreferences.getUserId(applicationContext)
            if (userId == "user_default" || userId.isEmpty()) {
                Log.d(TAG, "No user logged in, skipping")
                return Result.success()
            }

            // Fetch habits for user
            val habitRepository = HabitRepository.getInstance()
            val allHabits = habitRepository.getHabitsForUser(userId)

            if (allHabits.isEmpty()) {
                Log.d(TAG, "No habits found for user, skipping")
                return Result.success()
            }

            // Filter habits for today based on frequency
            val todayHabits = filterHabitsForToday(allHabits)

            if (todayHabits.isEmpty()) {
                Log.d(TAG, "No habits scheduled for today, skipping")
                return Result.success()
            }

            // Get today's date string
            val today = DateUtils.getCurrentDateString()

            // Find incomplete habits (not in completedDates for today)
            val incompleteHabits = todayHabits.filter { habit ->
                !habit.completedDates.contains(today)
            }

            if (incompleteHabits.isEmpty()) {
                Log.d(TAG, "All habits completed for today, no notification needed")
                return Result.success()
            }

            // Show notification for incomplete habits
            Log.d(TAG, "Found ${incompleteHabits.size} incomplete habits, showing notification")
            
            val habitNames = incompleteHabits.take(3).map { it.name }
            NotificationHelper.showReminderNotification(
                context = applicationContext,
                incompleteHabitCount = incompleteHabits.size,
                habitNames = habitNames
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in ReminderWorker", e)
            Result.failure()
        }
    }

    /**
     * Filter habits that are scheduled for today based on their frequency.
     * 
     * Frequency can be:
     * - ["Daily"] - runs every day
     * - ["Monday", "Wednesday", "Friday"] - runs on specific days
     * - ["Weekdays"] - Monday to Friday
     * - ["Weekends"] - Saturday and Sunday
     */
    private fun filterHabitsForToday(habits: List<Habit>): List<Habit> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Get full day name (e.g., "Monday", "Tuesday")
        val todayName = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }

        val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        return habits.filter { habit ->
            val frequency = habit.frequency
            when {
                // Daily habit
                frequency.contains("Daily") || frequency.contains("Everyday") -> true
                // Weekdays only
                frequency.contains("Weekdays") && isWeekday -> true
                // Weekends only
                frequency.contains("Weekends") && isWeekend -> true
                // Specific day of week
                frequency.contains(todayName) -> true
                // Default: not scheduled for today
                else -> false
            }
        }
    }
}
