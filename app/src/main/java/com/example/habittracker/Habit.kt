package com.example.habittracker

/**
 * Data model representing a habit.
 *
 * @property id Unique identifier for the habit
 * @property title Name of the habit
 * @property icon Emoji or icon identifier
 * @property isCompleted Whether the habit is completed for today
 */
data class Habit(
    val id: Int,
    val title: String,
    val icon: String = "📝",
    val isCompleted: Boolean = false
)

