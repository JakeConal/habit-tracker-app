package com.example.habittracker.data.repository

import com.example.habittracker.data.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository responsible for managing data
 * Provides an interface between ViewModel and Data Source (Database, Network, etc.)
 * Singleton pattern to share data across ViewModels
 */
class HabitRepository private constructor() {
    
    // Sample data - in real implementation, this would come from Database or API
    private val habits = mutableListOf<Habit>()
    
    // StateFlow to notify observers about changes
    private val _habitsFlow = MutableStateFlow<List<Habit>>(emptyList())
    
    companion object {
        @Volatile
        private var instance: HabitRepository? = null
        
        fun getInstance(): HabitRepository {
            return instance ?: synchronized(this) {
                instance ?: HabitRepository().also { instance = it }
            }
        }
    }
    
    /**
     * Get all habits as Flow to observe changes
     */
    fun getAllHabits(): Flow<List<Habit>> {
        return _habitsFlow.asStateFlow()
    }
    
    /**
     * Add a new habit
     */
    suspend fun addHabit(habit: Habit) {
        val newHabit = habit.copy(id = (habits.maxOfOrNull { it.id } ?: 0) + 1)
        habits.add(newHabit)
        _habitsFlow.value = habits.toList()
    }
    
    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            _habitsFlow.value = habits.toList()
        }
    }
    
    /**
     * Delete a habit
     */
    suspend fun deleteHabit(habitId: Long) {
        habits.removeIf { it.id == habitId }
        _habitsFlow.value = habits.toList()
    }

    /**
     * Get a habit by ID
     */
    fun getHabitById(habitId: Long): Habit? {
        return habits.find { it.id == habitId }
    }
}

