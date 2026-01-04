// UI models for HomeFragment
package com.example.habittracker.ui.dashboard

data class CalendarDay(
    val dayNumber: Int,
    val dayName: String,
    val isSelected: Boolean = false
)
