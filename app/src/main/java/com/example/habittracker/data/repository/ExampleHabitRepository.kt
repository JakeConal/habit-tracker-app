package com.example.habittracker.data.repository

import com.example.habittracker.data.models.ExampleHabit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository responsible for managing data
 * Provides an interface between ViewModel and Data Source (Database, Network, etc.)
 */
class ExampleHabitRepository {
    
    // Sample data - in real implementation, this would come from Database or API
    private val habits = mutableListOf<ExampleHabit>()
    
    /**
     * Get all habits as Flow to observe changes
     */
    fun getAllHabits(): Flow<List<ExampleHabit>> {
        return flowOf(habits.toList())
    }
    
    /**
     * Add a new habit
     */
    suspend fun addHabit(habit: ExampleHabit) {
        habits.add(habit.copy(id = habits.size.toLong() + 1))
    }
    
    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: ExampleHabit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
        }
    }
    
    /**
     * Delete a habit
     */
    suspend fun deleteHabit(habitId: Long) {
        habits.removeIf { it.id == habitId }
    }
}
