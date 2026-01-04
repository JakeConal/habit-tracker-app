package com.example.habittracker.ui.habit.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for preparing and managing data for the UI
 * This acts as an intermediary between View (Activity/Fragment) and Model (Data)
 */
class HabitListViewModel(
    private val repository: HabitRepository = HabitRepository.getInstance(),
    private val authRepository: AuthRepository = AuthRepository.getInstance()
) : ViewModel() {
    
    // State flow so UI can observe and update when data changes
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadHabits()
    }
    
    /**
     * Load the list of habits for current user
     */
    private fun loadHabits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = authRepository.getCurrentUser()?.uid
                if (userId != null) {
                    val habitList = repository.getHabitsForUser(userId)
                    _habits.value = habitList
                }
            } catch (e: Exception) {
                println("Error loading habits: ${e.message}")
                _habits.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Add a new habit for current user
     */
    fun addHabit(name: String, description: String, frequency: List<String>, categoryId: String = "", time: String = "") {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.uid
                if (userId != null) {
                    val habit = Habit(
                        userId = userId,
                        name = name,
                        description = description,
                        frequency = frequency,
                        createdAt = System.currentTimeMillis(),
                        categoryId = categoryId,
                        time = time
                    )
                    repository.addHabit(habit)
                    loadHabits()
                }
            } catch (e: Exception) {
                println("Error adding habit: ${e.message}")
            }
        }
    }
    
    /**
     * Toggle habit completion status
     */
    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.updateHabit(habit.copy(isCompleted = !habit.isCompleted))
                loadHabits()
            } catch (e: Exception) {
                println("Error toggling habit completion: ${e.message}")
            }
        }
    }
    
    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habitId)
                loadHabits()
            } catch (e: Exception) {
                println("Error deleting habit: ${e.message}")
            }
        }
    }
}
