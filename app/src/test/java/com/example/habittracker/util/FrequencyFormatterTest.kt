package com.example.habittracker.util

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FrequencyFormatter
 */
class FrequencyFormatterTest {
    
    @Test
    fun `formatFrequency returns Everyday for all 7 days`() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        assertEquals("Everyday", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency returns Weekdays for Mon-Fri`() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        assertEquals("Weekdays", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency returns Weekends for Sat-Sun`() {
        val days = listOf("Saturday", "Sunday")
        assertEquals("Weekends", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles consecutive days with range`() {
        val days = listOf("Monday", "Tuesday", "Wednesday")
        assertEquals("Mon - Wed", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles non-consecutive days`() {
        val days = listOf("Monday", "Wednesday")
        assertEquals("Mon, Wed", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles mix of consecutive and non-consecutive`() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Friday")
        assertEquals("Mon - Wed, Fri", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles unsorted input`() {
        val days = listOf("Friday", "Monday", "Wednesday")
        assertEquals("Mon, Wed, Fri", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency returns empty string for empty list`() {
        val days = emptyList<String>()
        assertEquals("", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles single day`() {
        val days = listOf("Monday")
        assertEquals("Mon", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles two consecutive days (less than 3)`() {
        val days = listOf("Monday", "Tuesday")
        assertEquals("Mon, Tue", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `formatFrequency handles complex pattern`() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Saturday")
        assertEquals("Mon - Thu, Sat", FrequencyFormatter.formatFrequency(days))
    }
    
    @Test
    fun `extension function works correctly`() {
        val days = listOf("Monday", "Wednesday", "Friday")
        assertEquals("Mon, Wed, Fri", days.formatFrequency())
    }
}
