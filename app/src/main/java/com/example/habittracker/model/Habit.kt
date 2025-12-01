package com.example.habittracker.model

/**
 * Data class representing a habit item.
 * @param id Unique identifier for the habit
 * @param title Display name of the habit
 * @param isCompleted Whether the habit has been completed today
 */
data class Habit(
    val id: Int,
    val title: String,
    val isCompleted: Boolean
)
