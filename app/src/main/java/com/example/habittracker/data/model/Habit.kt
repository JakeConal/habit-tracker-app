package com.example.habittracker.data.model

/**
 * Data class representing a Habit
 * This is the Model in MVVM architecture
 */
data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: String = "Daily", // Daily, Weekly, Monthly
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val iconRes: Int = 0, // Icon resource ID
    val iconBackgroundRes: Int = 0 // Icon background resource ID
)

