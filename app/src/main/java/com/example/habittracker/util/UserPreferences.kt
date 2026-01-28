package com.example.habittracker.util

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREF_NAME = "habit_tracker_prefs"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_AVATAR = "user_avatar"
    private const val KEY_USER_ID = "user_id"

    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    // Reminder settings
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun saveUserName(context: Context, name: String) {
        getPreferences(context).edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(context: Context): String {
        return getPreferences(context).getString(KEY_USER_NAME, "You") ?: "You"
    }

    fun saveUserAvatar(context: Context, avatarUrl: String) {
        getPreferences(context).edit().putString(KEY_USER_AVATAR, avatarUrl).apply()
    }

    fun getUserAvatar(context: Context): String {
        return getPreferences(context).getString(KEY_USER_AVATAR, "") ?: ""
    }

    fun saveUserId(context: Context, userId: String) {
        getPreferences(context).edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String {
        return getPreferences(context).getString(KEY_USER_ID, "user_default") ?: "user_default"
    }

    fun clearUserData(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    // ==================== Reminder Settings ====================

    /**
     * Set daily reminder enabled/disabled
     */
    fun setReminderEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    /**
     * Check if daily reminder is enabled
     */
    fun isReminderEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_REMINDER_ENABLED, false)
    }

    /**
     * Save reminder time (hour in 24h format)
     */
    fun setReminderHour(context: Context, hour: Int) {
        getPreferences(context).edit().putInt(KEY_REMINDER_HOUR, hour).apply()
    }

    /**
     * Get reminder hour (default 20:00 / 8 PM)
     */
    fun getReminderHour(context: Context): Int {
        return getPreferences(context).getInt(KEY_REMINDER_HOUR, 20)
    }

    /**
     * Save reminder minute
     */
    fun setReminderMinute(context: Context, minute: Int) {
        getPreferences(context).edit().putInt(KEY_REMINDER_MINUTE, minute).apply()
    }

    /**
     * Get reminder minute (default 0)
     */
    fun getReminderMinute(context: Context): Int {
        return getPreferences(context).getInt(KEY_REMINDER_MINUTE, 0)
    }

    /**
     * Save reminder time (hour and minute together)
     */
    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        getPreferences(context).edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    /**
     * Get formatted reminder time string (e.g., "20:00")
     */
    fun getFormattedReminderTime(context: Context): String {
        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)
        return String.format("%02d:%02d", hour, minute)
    }
}
