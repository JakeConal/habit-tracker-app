package com.example.habittracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"

    /**
     * Get current date as string in yyyy-MM-dd format
     */
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Get yesterday's date as string in yyyy-MM-dd format
     */
    fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(calendar.time)
    }

    /**
     * Check if a date string is today
     */
    fun isToday(dateString: String): Boolean {
        return dateString == getCurrentDateString()
    }
}

