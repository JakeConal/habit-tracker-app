package com.example.habittracker.utils

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREF_NAME = "habit_tracker_prefs"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_AVATAR = "user_avatar"
    private const val KEY_USER_ID = "user_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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
}
