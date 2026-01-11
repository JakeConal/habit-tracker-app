package com.example.habittracker.util

/**
 * Example usage of the FrequencyFormatter
 * 
 * This demonstrates the various formatting options available
 */
object FrequencyFormatterExamples {
    
    fun demonstrateUsage() {
        // Example 1: All 7 days
        val everyday = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        println(everyday.formatFrequency())  // Output: "Everyday"
        
        // Example 2: Weekdays only
        val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        println(weekdays.formatFrequency())  // Output: "Weekdays"
        
        // Example 3: Weekends only
        val weekends = listOf("Saturday", "Sunday")
        println(weekends.formatFrequency())  // Output: "Weekends"
        
        // Example 4: Consecutive days (3+ days)
        val consecutive = listOf("Monday", "Tuesday", "Wednesday")
        println(consecutive.formatFrequency())  // Output: "Mon - Wed"
        
        // Example 5: Non-consecutive days
        val nonConsecutive = listOf("Monday", "Wednesday")
        println(nonConsecutive.formatFrequency())  // Output: "Mon, Wed"
        
        // Example 6: Mix of consecutive and non-consecutive
        val mixed = listOf("Monday", "Tuesday", "Wednesday", "Friday")
        println(mixed.formatFrequency())  // Output: "Mon - Wed, Fri"
        
        // Example 7: Unsorted input (will be sorted automatically)
        val unsorted = listOf("Friday", "Monday", "Wednesday")
        println(unsorted.formatFrequency())  // Output: "Mon, Wed, Fri"
        
        // Example 8: Complex pattern
        val complex = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Saturday")
        println(complex.formatFrequency())  // Output: "Mon - Thu, Sat"
        
        // Example 9: Single day
        val singleDay = listOf("Monday")
        println(singleDay.formatFrequency())  // Output: "Mon"
        
        // Example 10: Two consecutive days (less than 3, shown separately)
        val twoConsecutive = listOf("Monday", "Tuesday")
        println(twoConsecutive.formatFrequency())  // Output: "Mon, Tue"
    }
}
