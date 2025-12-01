package com.example.habittracker.model

/**
 * Data class representing a date item in the date selector.
 * @param dayOfWeek Short day name (e.g., "Mon", "Tue")
 * @param dayNumber Day of the month (e.g., "15")
 * @param isSelected Whether this date is currently selected
 */
data class DateItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val isSelected: Boolean = false
)
