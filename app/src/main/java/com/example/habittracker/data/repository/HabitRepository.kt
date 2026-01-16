package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing Habit data, combining local caching with Firestore persistence
 */
class HabitRepository private constructor() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: Flow<List<Habit>> = _habits.asStateFlow()

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
     * Get all habits for a specific user
     */
    suspend fun getHabitsForUser(userId: String): List<Habit> {
        return try {
            val habits = FirestoreManager.getCollectionWhere(
                collectionName = Habit.COLLECTION_NAME,
                field = "userId",
                value = userId,
                mapper = { document -> Habit.fromDocument(document) }
            )
            _habits.value = habits
            habits
        } catch (e: Exception) {
            println("Error getting habits for user: ${e.message}")
            emptyList()
        }
    }

    /**
     * Add a new habit
     */
    suspend fun addHabit(habit: Habit): String? {
        return try {
            val habitId = FirestoreManager.addDocument(
                Habit.COLLECTION_NAME,
                habit.toMap()
            )
            if (habitId != null) {
                // Refresh the habits list
                getHabitsForUser(habit.userId)
            }
            habitId
        } catch (e: Exception) {
            println("Error adding habit: ${e.message}")
            null
        }
    }

    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: Habit): Boolean {
        return try {
            val success = FirestoreManager.updateDocument(
                Habit.COLLECTION_NAME,
                habit.id,
                habit.toMap()
            )
            if (success) {
                // Refresh the habits list
                getHabitsForUser(habit.userId)
            }
            success
        } catch (e: Exception) {
            println("Error updating habit: ${e.message}")
            false
        }
    }

    /**
     * Delete a habit
     */
    suspend fun deleteHabit(habitId: String): Boolean {
        return try {
            FirestoreManager.deleteDocument(Habit.COLLECTION_NAME, habitId)
        } catch (e: Exception) {
            println("Error deleting habit: ${e.message}")
            false
        }
    }

    /**
     * Delete all habits for a user
     */
    suspend fun deleteHabitsForUser(userId: String): Boolean {
        return try {
            val habits = getHabitsForUser(userId)
            var allSuccess = true
            for (habit in habits) {
                if (!deleteHabit(habit.id)) {
                    allSuccess = false
                }
            }
            if (allSuccess) {
                _habits.value = emptyList()
            }
            allSuccess
        } catch (e: Exception) {
            println("Error deleting habits for user: ${e.message}")
            false
        }
    }

    /**
     * Get a habit by ID
     */
    suspend fun getHabitById(habitId: String): Habit? {
        return try {
            val document = FirestoreManager.getDocument(Habit.COLLECTION_NAME, habitId)
            document?.let { Habit.fromDocument(it) }
        } catch (e: Exception) {
            println("Error getting habit by ID: ${e.message}")
            null
        }
    }

    /**
     * Toggle habit completion status
     */
    suspend fun toggleHabitCompletion(habitId: String): Boolean {
        return try {
            val habit = getHabitById(habitId)
            if (habit != null) {
                val updatedHabit = habit.copy(isCompleted = !habit.isCompleted)
                updateHabit(updatedHabit)
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error toggling habit completion: ${e.message}")
            false
        }
    }

    /**
     * Mark habit as completed for today
     */
    suspend fun markHabitCompleted(habitId: String, date: String): Boolean {
        return try {
            val habit = getHabitById(habitId)
            if (habit != null) {
                val completedDates = habit.completedDates.toMutableList()
                if (!completedDates.contains(date)) {
                    completedDates.add(date)
                    val updatedHabit = habit.copy(
                        isCompleted = true,
                        completedDates = completedDates,
                        streak = habit.streak + 1
                    )
                    updateHabit(updatedHabit)
                } else {
                    true // Already completed
                }
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error marking habit as completed: ${e.message}")
            false
        }
    }

    /**
     * Get habits by frequency
     */
    suspend fun getHabitsByFrequency(userId: String, frequency: List<String>): List<Habit> {
        return try {
            val allHabits = getHabitsForUser(userId)
            allHabits.filter { it.frequency == frequency }
        } catch (e: Exception) {
            println("Error getting habits by frequency: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get the count of habits for a specific category
     */
    suspend fun getHabitCountByCategory(userId: String, categoryId: String): Int {
        return try {
            val habits = FirestoreManager.getCollectionWhere(
                collectionName = Habit.COLLECTION_NAME,
                field = "userId",
                value = userId,
                mapper = { document -> Habit.fromDocument(document) }
            )
            habits.count { it.categoryId == categoryId }
        } catch (e: Exception) {
            println("Error getting habit count by category: ${e.message}")
            0
        }
    }

    /**
     * Clear all local cache
     */
    fun clearCache() {
        _habits.value = emptyList()
    }
}
