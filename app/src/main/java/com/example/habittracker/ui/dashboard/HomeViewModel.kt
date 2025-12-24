package com.example.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HomeViewModel - ViewModel for Home/Dashboard screen
 * Manages the UI state and business logic for displaying habits
 */
class HomeViewModel : ViewModel() {

    private val repository = HabitRepository.getInstance()

    // UI State
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHabits()
    }

    /**
     * Load all habits from repository
     */
    fun loadHabits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllHabits().collect { habitsList ->
                    _habits.value = habitsList
                }
            } catch (e: Exception) {
                // Handle error
                _habits.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle habit completion status
     */
    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            try {
                val updatedHabit = habit.copy(isCompleted = !habit.isCompleted)
                repository.updateHabit(updatedHabit)
                loadHabits() // Reload habits to reflect changes
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habitId)
                loadHabits() // Reload habits to reflect changes
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
