package com.example.habittracker.util

/**
 * Object containing constants used throughout the application
 */
object Constants {
    
    // Database constants
    const val DATABASE_NAME = "habit_tracker_db"
    const val DATABASE_VERSION = 1
    
    // Frequency types
    const val FREQUENCY_DAILY = "Daily"
    const val FREQUENCY_WEEKLY = "Weekly"
    const val FREQUENCY_MONTHLY = "Monthly"
    
    // SharedPreferences keys
    const val PREFS_NAME = "habit_tracker_prefs"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_USER_ID = "user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_LANGUAGE = "language"
    
    // Notification channels
    const val NOTIFICATION_CHANNEL_REMINDER = "reminder_channel"
    const val NOTIFICATION_CHANNEL_STREAK = "streak_channel"
    const val NOTIFICATION_CHANNEL_CHALLENGE = "challenge_channel"
    
    // Intent extras
    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_CHALLENGE_ID = "extra_challenge_id"
    const val EXTRA_USER_ID = "extra_user_id"
    
    // Request codes
    const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1001
    const val REQUEST_CODE_ALARM_PERMISSION = 1002
    
    // WorkManager tags
    const val WORK_TAG_DAILY_QUOTE = "daily_quote_work"
    const val WORK_TAG_STREAK_CHECK = "streak_check_work"
    const val WORK_TAG_DATA_SYNC = "data_sync_work"
    
    // Pomodoro defaults
    const val DEFAULT_POMODORO_WORK_DURATION = 25 // minutes
    const val DEFAULT_POMODORO_BREAK_DURATION = 5 // minutes
    const val DEFAULT_POMODORO_LONG_BREAK_DURATION = 15 // minutes
    const val DEFAULT_POMODORO_SESSIONS_BEFORE_LONG_BREAK = 4
}

