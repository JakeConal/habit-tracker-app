package com.example.habittracker.ui.habit.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.util.DateUtils
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
     * Loads all fields directly from the Habit model, not from description string
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
                    
                    // Load frequency directly from habit.frequency field
                    _frequency.value = habit.frequency
                    
                    // Load quantity and unit directly from habit fields
                    _quantity.value = habit.quantity
                    _measurement.value = habit.unit

                    // Load Pomodoro settings
                    _isPomodoroRequired.value = habit.isPomodoroRequired
                    _focusDuration.value = habit.focusDuration
                    _shortBreak.value = habit.shortBreak
                    _longBreak.value = habit.longBreak
                    _totalSessions.value = habit.totalSessions

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

                // Create updated habit object
                val updatedHabit = currentHabit.copy(
                    name = _title.value,
                    quantity = _quantity.value,
                    unit = _measurement.value,
                    frequency = _frequency.value,
                    categoryId = _categoryId.value,
                    isPomodoroRequired = _isPomodoroRequired.value,
                    focusDuration = _focusDuration.value,
                    shortBreak = _shortBreak.value,
                    longBreak = _longBreak.value,
                    totalSessions = _totalSessions.value
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
     * Complete the habit without Pomodoro
     */
    fun completeHabit() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentHabit = _habit.value ?: return@launch

                // Restriction: Cannot complete if it's already completed for today
                val today = DateUtils.getCurrentDateString()
                if (currentHabit.completedDates.contains(today)) {
                    _error.emit("Habit already completed for today")
                    return@launch
                }

                repository.toggleHabitCompletion(currentHabit.id)

                _habitUpdated.emit(true)
            } catch (e: Exception) {
                _error.emit(e.message ?: "Failed to complete habit")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
