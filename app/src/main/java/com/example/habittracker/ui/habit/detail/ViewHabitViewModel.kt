package com.example.habittracker.ui.habit.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewHabitViewModel - ViewModel for View/Edit Habit screen
 * Manages the UI state and business logic for viewing and editing a habit
 */
class ViewHabitViewModel : ViewModel() {

    private val repository = HabitRepository.getInstance()
    private val authRepository = AuthRepository.getInstance()
    private val categoryRepository = CategoryRepository.getInstance()

    // Current habit being viewed/edited
    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()

    // Current category
    private val _category = MutableStateFlow<com.example.habittracker.data.model.Category?>(null)
    val category: StateFlow<com.example.habittracker.data.model.Category?> = _category.asStateFlow()

    // UI State
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _categoryId = MutableStateFlow("")
    val categoryId: StateFlow<String> = _categoryId.asStateFlow()

    private val _quantity = MutableStateFlow(30)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _measurement = MutableStateFlow("Mins")
    val measurement: StateFlow<String> = _measurement.asStateFlow()

    private val _frequency = MutableStateFlow<List<String>>(emptyList())
    val frequency: StateFlow<List<String>> = _frequency.asStateFlow()

    private val _time = MutableStateFlow("5:00 - 12:00")
    val time: StateFlow<String> = _time.asStateFlow()

    // Events
    private val _habitUpdated = MutableSharedFlow<Boolean>()
    val habitUpdated: SharedFlow<Boolean> = _habitUpdated.asSharedFlow()

    private val _habitDeleted = MutableSharedFlow<Boolean>()
    val habitDeleted: SharedFlow<Boolean> = _habitDeleted.asSharedFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load habit by ID
     */
    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habit = repository.getHabitById(habitId)
                if (habit != null) {
                    _habit.value = habit
                    _title.value = habit.name
                    _categoryId.value = habit.categoryId
                    _frequency.value = habit.frequency
                    _time.value = habit.time
                    // Parse description for other fields if available
                    parseDescription(habit.description)

                    // Load category details
                    loadCategory(habit.categoryId)
                } else {
                    _error.emit("Habit not found")
                }
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to load habit")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load category by ID
     */
    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategoryById(categoryId)
                _category.value = category
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to load category")
            }
        }
    }

    /**
     * Parse habit description to extract quantity, measurement, time
     */
    private fun parseDescription(description: String) {
        // Description format: "30 Mins, Everyday, 5:00 - 12:00"
        val parts = description.split(", ")
        if (parts.isNotEmpty()) {
            val quantityMeasurement = parts[0].split(" ")
            if (quantityMeasurement.size >= 2) {
                _quantity.value = quantityMeasurement[0].toIntOrNull() ?: 30
                _measurement.value = quantityMeasurement[1]
            }
        }
        if (parts.size >= 3) {
            _time.value = parts[2]
        }
    }

    /**
     * Update habit title
     */
    fun updateTitle(title: String) {
        _title.value = title
    }

    /**
     * Update category ID
     */
    fun updateCategoryId(categoryId: String) {
        _categoryId.value = categoryId
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
    fun updateFrequency(frequency: List<String>) {
        _frequency.value = frequency
    }

    /**
     * Update time range
     */
    fun updateTime(time: String) {
        _time.value = time
    }

    /**
     * Save/Update the habit with current state
     */
    fun saveHabit() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Validate data
                if (_title.value.isBlank()) {
                    _error.emit("Habit title cannot be empty")
                    return@launch
                }

                val currentHabit = _habit.value
                if (currentHabit == null) {
                    _error.emit("No habit to update")
                    return@launch
                }

                // Create updated habit description
                val description = buildHabitDescription()

                // Create updated habit object
                val updatedHabit = currentHabit.copy(
                    name = _title.value,
                    description = description,
                    frequency = _frequency.value,
                    categoryId = _categoryId.value,
                    time = _time.value
                )

                // Update in repository
                repository.updateHabit(updatedHabit)

                // Emit success event
                _habitUpdated.emit(true)
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to update habit")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete the current habit
     */
    fun deleteHabit() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentHabit = _habit.value
                if (currentHabit == null) {
                    _error.emit("No habit to delete")
                    return@launch
                }

                // Delete from repository
                repository.deleteHabit(currentHabit.id)

                // Emit success event
                _habitDeleted.emit(true)
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to delete habit")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Build habit description from selected options
     */
    private fun buildHabitDescription(): String {
        return "${_quantity.value} ${_measurement.value}, ${_frequency.value}, ${_time.value}"
    }
}
