package com.example.habittracker.ui.habit.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CreateHabitViewModel - ViewModel for Create Habit screen
 * Manages the UI state and business logic for creating a new habit
 */
class CreateHabitViewModel : ViewModel() {

    private val repository = HabitRepository()

    // UI State
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _category = MutableStateFlow("Reading")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _quantity = MutableStateFlow(30)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _measurement = MutableStateFlow("Mins")
    val measurement: StateFlow<String> = _measurement.asStateFlow()

    private val _frequency = MutableStateFlow("Everyday")
    val frequency: StateFlow<String> = _frequency.asStateFlow()

    private val _time = MutableStateFlow("5:00 - 12:00")
    val time: StateFlow<String> = _time.asStateFlow()

    // Events
    private val _habitCreated = MutableSharedFlow<Boolean>()
    val habitCreated: SharedFlow<Boolean> = _habitCreated.asSharedFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Update habit title
     */
    fun updateTitle(title: String) {
        _title.value = title
    }

    /**
     * Update habit category
     */
    fun updateCategory(category: String) {
        _category.value = category
    }

    /**
     * Update quantity
     */
    fun updateQuantity(quantity: Int) {
        _quantity.value = quantity
    }

    /**
     * Update measurement unit
     */
    fun updateMeasurement(measurement: String) {
        _measurement.value = measurement
    }

    /**
     * Update frequency
     */
    fun updateFrequency(frequency: String) {
        _frequency.value = frequency
    }

    /**
     * Update time range
     */
    fun updateTime(time: String) {
        _time.value = time
    }

    /**
     * Create a new habit with the current state
     */
    fun createHabit() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Validate data
                if (_title.value.isBlank()) {
                    _error.emit("Habit title cannot be empty")
                    return@launch
                }

                // Create habit description from selected options
                val description = buildHabitDescription()

                // Create habit object
                val habit = Habit(
                    name = _title.value,
                    description = description,
                    frequency = _frequency.value,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )

                // Save to repository
                repository.addHabit(habit)

                // Emit success event
                _habitCreated.emit(true)
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to create habit")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Build a descriptive text from selected options
     */
    private fun buildHabitDescription(): String {
        return buildString {
            append("Category: ${_category.value}")
            append(" • ")
            append("Goal: ${_quantity.value} ${_measurement.value}")
            append(" • ")
            append("Frequency: ${_frequency.value}")
            append(" • ")
            append("Time: ${_time.value}")
        }
    }

    /**
     * Reset all fields to default values
     */
    fun resetForm() {
        _title.value = ""
        _category.value = "Reading"
        _quantity.value = 30
        _measurement.value = "Mins"
        _frequency.value = "Everyday"
        _time.value = "5:00 - 12:00"
    }
}
