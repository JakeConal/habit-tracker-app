package com.example.habittracker.data.model

/**
 * Request model for AI-generated motivational quotes
 */
data class QuoteRequest(
    val totalHabits: Int,
    val completedYesterday: Int,
    val bestStreak: Int,
    val bestStreakHabitName: String,
    val averageStreak: Int
)
