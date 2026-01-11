package com.example.habittracker.util

/**
 * Utility object for formatting frequency lists into human-readable strings
 */
object FrequencyFormatter {
    
    private val WEEK_ORDER = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val DAY_ABBREVIATIONS = mapOf(
        "Monday" to "Mon",
        "Tuesday" to "Tue",
        "Wednesday" to "Wed",
        "Thursday" to "Thu",
        "Friday" to "Fri",
        "Saturday" to "Sat",
        "Sunday" to "Sun"
    )
    
    /**
     * Formats a list of selected days into a human-readable string
     * 
     * Examples:
     * - All 7 days -> "Everyday"
     * - Mon-Fri -> "Weekdays"
     * - Sat-Sun -> "Weekends"
     * - Monday, Tuesday, Wednesday -> "Mon - Wed"
     * - Monday, Wednesday -> "Mon, Wed"
     * - Monday, Tuesday, Wednesday, Friday -> "Mon - Wed, Fri"
     * 
     * @param selectedDays List of day names (e.g., ["Monday", "Wednesday"])
     * @return Formatted frequency string
     */
    fun formatFrequency(selectedDays: List<String>): String {
        // Handle empty list
        if (selectedDays.isEmpty()) return ""
        
        // Sort days based on week order
        val sortedDays = selectedDays.sortedBy { day -> 
            WEEK_ORDER.indexOf(day).takeIf { it >= 0 } ?: Int.MAX_VALUE
        }
        
        // Check for special cases
        when {
            sortedDays.size == 7 && sortedDays.containsAll(WEEK_ORDER) -> return "Everyday"
            sortedDays.size == 5 && sortedDays.containsAll(WEEK_ORDER.subList(0, 5)) -> return "Weekdays"
            sortedDays.size == 2 && sortedDays.containsAll(WEEK_ORDER.subList(5, 7)) -> return "Weekends"
        }
        
        // Build formatted string with consecutive day ranges
        val result = mutableListOf<String>()
        var i = 0
        
        while (i < sortedDays.size) {
            val currentDay = sortedDays[i]
            val currentIndex = WEEK_ORDER.indexOf(currentDay)
            
            if (currentIndex == -1) {
                // Invalid day name, just add it as-is
                result.add(currentDay)
                i++
                continue
            }
            
            // Find consecutive days
            var consecutiveEnd = i
            while (consecutiveEnd < sortedDays.size - 1) {
                val nextDay = sortedDays[consecutiveEnd + 1]
                val nextIndex = WEEK_ORDER.indexOf(nextDay)
                
                if (nextIndex == currentIndex + (consecutiveEnd - i) + 1) {
                    consecutiveEnd++
                } else {
                    break
                }
            }
            
            // Check if we have 3 or more consecutive days
            val consecutiveCount = consecutiveEnd - i + 1
            if (consecutiveCount >= 3) {
                // Format as range
                val startAbbr = DAY_ABBREVIATIONS[sortedDays[i]] ?: sortedDays[i]
                val endAbbr = DAY_ABBREVIATIONS[sortedDays[consecutiveEnd]] ?: sortedDays[consecutiveEnd]
                result.add("$startAbbr - $endAbbr")
                i = consecutiveEnd + 1
            } else {
                // Add individual day
                val abbr = DAY_ABBREVIATIONS[currentDay] ?: currentDay
                result.add(abbr)
                i++
            }
        }
        
        return result.joinToString(", ")
    }
}

/**
 * Extension function for List<String> to format frequency
 */
fun List<String>.formatFrequency(): String {
    return FrequencyFormatter.formatFrequency(this)
}
