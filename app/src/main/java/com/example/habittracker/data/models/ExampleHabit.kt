package com.example.habittracker.data.models

/**
 * Data class representing a Habit
 * This is the Model in MVVM architecture
 */
data class ExampleHabit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: String = "Daily", // Daily, Weekly, Monthly
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
