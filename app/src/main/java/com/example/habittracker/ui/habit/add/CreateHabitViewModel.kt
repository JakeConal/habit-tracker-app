package com.example.habittracker.ui.habit.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.Category
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
 * CreateHabitViewModel - ViewModel for Create Habit screen
 * Manages the UI state and business logic for creating a new habit
 */
class CreateHabitViewModel : ViewModel() {

    private val repository = HabitRepository.getInstance()
    private val authRepository = AuthRepository.getInstance()
    private val categoryRepository = CategoryRepository.getInstance()

    // UI State
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    // Current category (single source of truth)
    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _categoryId = MutableStateFlow("")
    val categoryId: StateFlow<String> = _categoryId.asStateFlow()

    private val _quantity = MutableStateFlow(30)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _measurement = MutableStateFlow("Mins")
    val measurement: StateFlow<String> = _measurement.asStateFlow()

    private val _frequency = MutableStateFlow<List<String>>(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
    val frequency: StateFlow<List<String>> = _frequency.asStateFlow()

    // Pomodoro settings
    private val _isPomodoroRequired = MutableStateFlow(false)
    val isPomodoroRequired: StateFlow<Boolean> = _isPomodoroRequired.asStateFlow()

    private val _focusDuration = MutableStateFlow(25)
    val focusDuration: StateFlow<Int> = _focusDuration.asStateFlow()

    private val _shortBreak = MutableStateFlow(5)
    val shortBreak: StateFlow<Int> = _shortBreak.asStateFlow()

    private val _longBreak = MutableStateFlow(15)
    val longBreak: StateFlow<Int> = _longBreak.asStateFlow()

    private val _totalSessions = MutableStateFlow(4)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()


    // Events
    private val _habitCreated = MutableSharedFlow<Boolean>()
    val habitCreated: SharedFlow<Boolean> = _habitCreated.asSharedFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load category by ID from repository
     * This is the single source of truth for category data
     */
    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategoryById(categoryId)
                _category.value = category
                _categoryId.value = categoryId
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to load category")
            }
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
     * Update Pomodoro settings
     */
    fun updatePomodoroRequired(required: Boolean) {
        _isPomodoroRequired.value = required
    }

    fun updateFocusDuration(duration: Int) {
        _focusDuration.value = duration
    }

    fun updateShortBreak(duration: Int) {
        _shortBreak.value = duration
    }

    fun updateLongBreak(duration: Int) {
        _longBreak.value = duration
    }

    fun updateTotalSessions(sessions: Int) {
        _totalSessions.value = sessions
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

                // Get current user ID
                val currentUserId = authRepository.getCurrentUser()?.uid
                if (currentUserId == null) {
                    _error.emit("User not authenticated")
                    return@launch
                }

                // Create habit object with Firebase structure
                val habit = Habit(
                    userId = currentUserId,
                    name = _title.value,
                    quantity = _quantity.value,
                    unit = _measurement.value,
                    frequency = _frequency.value,
                    createdAt = System.currentTimeMillis(),
                    categoryId = _categoryId.value,
                    completedDates = emptyList(),
                    streak = 0,
                    isPomodoroRequired = _isPomodoroRequired.value,
                    focusDuration = _focusDuration.value,
                    shortBreak = _shortBreak.value,
                    longBreak = _longBreak.value,
                    totalSessions = _totalSessions.value
                )

                // Save to repository
                val habitId = repository.addHabit(habit)

                if (habitId != null) {
                    // Emit success event
                    _habitCreated.emit(true)
                } else {
                    _error.emit("Failed to create habit")
                }
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to create habit")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Build a descriptive text from selected options
     * NOTE: This method is deprecated as we now store fields separately.
     */
    private fun buildHabitDescription(): String {
        return ""
    }

    /**
     * Reset all fields to default values
     */
    fun resetForm() {
        _title.value = ""
        _category.value = null
        _quantity.value = 30
        _measurement.value = "Mins"
        _frequency.value = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        _isPomodoroRequired.value = false
        _focusDuration.value = 25
        _shortBreak.value = 5
        _longBreak.value = 15
        _totalSessions.value = 4
    }
}
