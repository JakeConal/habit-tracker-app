package com.example.habittracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.models.ExampleHabit
import com.example.habittracker.data.repository.ExampleHabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for preparing and managing data for the UI
 * This acts as an intermediary between View (Activity/Fragment) and Model (Data)
 */
class ExampleHabitViewModel(
    private val repository: ExampleHabitRepository = ExampleHabitRepository()
) : ViewModel() {
    
    // State flow so UI can observe and update when data changes
    private val _habits = MutableStateFlow<List<ExampleHabit>>(emptyList())
    val habits: StateFlow<List<ExampleHabit>> = _habits
    
    init {
        loadHabits()
    }
    
    /**
     * Load the list of habits
     */
    private fun loadHabits() {
        viewModelScope.launch {
            repository.getAllHabits().collect { habitList ->
                _habits.value = habitList
            }
        }
    }
    
    /**
     * Add a new habit
     */
    fun addHabit(name: String, description: String, frequency: String) {
        viewModelScope.launch {
            val habit = ExampleHabit(
                name = name,
                description = description,
                frequency = frequency
            )
            repository.addHabit(habit)
            loadHabits()
        }
    }
    
    /**
     * Toggle habit completion status
     */
    fun toggleHabitCompletion(habit: ExampleHabit) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isCompleted = !habit.isCompleted))
            loadHabits()
        }
    }
    
    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            loadHabits()
        }
    }
}
