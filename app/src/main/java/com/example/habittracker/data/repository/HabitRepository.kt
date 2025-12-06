package com.example.habittracker.data.repository

import com.example.habittracker.data.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository responsible for managing data
 * Provides an interface between ViewModel and Data Source (Database, Network, etc.)
 */
class HabitRepository {
    
    // Sample data - in real implementation, this would come from Database or API
    private val habits = mutableListOf<Habit>()
    
    /**
     * Get all habits as Flow to observe changes
     */
    fun getAllHabits(): Flow<List<Habit>> {
        return flowOf(habits.toList())
    }
    
    /**
     * Add a new habit
     */
    suspend fun addHabit(habit: Habit) {
        habits.add(habit.copy(id = habits.size.toLong() + 1))
    }
    
    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: Habit) {
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

