package com.example.habittracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.habittracker.R
import com.example.habittracker.ui.main.MainActivity

/**
 * Helper class for creating and managing notifications
 */
object NotificationHelper {

    // Notification channel for habit reminders
    const val REMINDER_CHANNEL_ID = "habit_reminder_channel"
    private const val REMINDER_CHANNEL_NAME = "Habit Reminders"
    private const val REMINDER_CHANNEL_DESCRIPTION = "Daily reminders to complete your habits"

    // Default notification channel for social interactions
    const val DEFAULT_CHANNEL_ID = "habit_tracker_default_channel"
    private const val DEFAULT_CHANNEL_NAME = "Habit Tracker Notifications"
    private const val DEFAULT_CHANNEL_DESCRIPTION = "Notifications for likes, comments, and shares"

    // Notification IDs
    const val REMINDER_NOTIFICATION_ID = 1001

    /**
     * Create all notification channels (required for Android O+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Reminder Channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = REMINDER_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            // Default Social Channel
            val defaultChannel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = DEFAULT_CHANNEL_DESCRIPTION
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(defaultChannel)
        }
    }

    /**
     * Show reminder notification for incomplete habits
     * @param context Application context
     * @param incompleteHabitCount Number of incomplete habits
     * @param habitNames List of incomplete habit names (max 3 shown)
     */
    fun showReminderNotification(
        context: Context,
        incompleteHabitCount: Int,
        habitNames: List<String> = emptyList()
    ) {
        // Create notification channel if not exists
        createNotificationChannels(context)

        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_reminder", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification content
        val title = context.getString(R.string.reminder_notification_title)
        val contentText = buildNotificationContent(context, incompleteHabitCount, habitNames)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(REMINDER_NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * Build notification content based on incomplete habits
     */
    private fun buildNotificationContent(
        context: Context,
        count: Int,
        habitNames: List<String>
    ): String {
        return when {
            count == 1 && habitNames.isNotEmpty() -> {
                context.getString(R.string.reminder_single_habit, habitNames.first())
            }
            count <= 3 && habitNames.isNotEmpty() -> {
                val names = habitNames.joinToString(", ")
                context.getString(R.string.reminder_few_habits, names)
            }
            else -> {
                context.getString(R.string.reminder_many_habits, count)
            }
        }
    }

    /**
     * Cancel reminder notification
     */
    fun cancelReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(REMINDER_NOTIFICATION_ID)
    }
}
