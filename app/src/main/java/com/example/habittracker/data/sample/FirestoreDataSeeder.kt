package com.example.habittracker.data.sample

import com.example.habittracker.R
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.data.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Seeds sample data to Firestore for testing and demo purposes
 */
object FirestoreDataSeeder {

    private val habitRepository = HabitRepository.getInstance()
    private val userRepository = FirestoreUserRepository.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Seed sample habits for the current user
     */
    suspend fun seedSampleHabits(): Boolean {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return false

            val sampleHabits = getSampleHabits(currentUserId)

            // Check if user already has habits to avoid duplicating
            val existingHabits = habitRepository.getHabitsForUser(currentUserId)
            if (existingHabits.isNotEmpty()) {
                println("User already has habits, skipping sample data seeding")
                return true
            }

            // Add sample habits
            var allSuccess = true
            sampleHabits.forEach { habit ->
                val result = habitRepository.addHabit(habit)
                if (result == null) {
                    allSuccess = false
                    println("Failed to add habit: ${habit.name}")
                }
            }

            println("Sample habits seeding completed. Success: $allSuccess")
            allSuccess
        } catch (e: Exception) {
            println("Error seeding sample habits: ${e.message}")
            false
        }
    }

    /**
     * Create sample user profile if it doesn't exist
     */
    suspend fun seedSampleUserProfile(): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false

            // Check if user profile exists in Firestore
            val existingUser = userRepository.getUserById(currentUser.uid)
            if (existingUser != null) {
                println("User profile already exists, skipping sample data seeding")
                return true
            }

            // Create sample user profile
            val sampleUser = User(
                id = currentUser.uid,
                name = currentUser.displayName ?: "Sample User",
                email = currentUser.email,
                avatarUrl = currentUser.photoUrl?.toString(),
                points = 150,
                rank = 1,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )

            val success = userRepository.createOrUpdateUser(sampleUser)
            println("Sample user profile seeding completed. Success: $success")
            success
        } catch (e: Exception) {
            println("Error seeding sample user profile: ${e.message}")
            false
        }
    }

    /**
     * Generate sample habits for a user
     */
    private fun getSampleHabits(userId: String): List<Habit> {
        return listOf(
            Habit(
                userId = userId,
                name = "Read for 30 minutes",
                description = "Read books, articles, or educational content for at least 30 minutes",
                frequency = listOf("Daily"),
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                categoryId = "reading",
                completedDates = emptyList(),
                streak = 3,
                time = "9:00 - 10:00"
            ),
            Habit(
                userId = userId,
                name = "Morning Exercise",
                description = "Do 30 minutes of physical exercise in the morning",
                frequency = listOf("Daily"),
                isCompleted = true,
                createdAt = System.currentTimeMillis(),
                categoryId = "health",
                completedDates = emptyList(),
                streak = 7,
                time = "6:00 - 7:00"
            ),
            Habit(
                userId = userId,
                name = "Drink 8 glasses of water",
                description = "Stay hydrated by drinking at least 8 glasses of water daily",
                frequency = listOf("Daily"),
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                categoryId = "health",
                completedDates = emptyList(),
                streak = 2,
                time = "8:00 - 18:00"
            ),
            Habit(
                userId = userId,
                name = "Meditate",
                description = "Practice mindfulness and meditation for 15 minutes",
                frequency = listOf("Daily"),
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                categoryId = "mindfulness",
                completedDates = emptyList(),
                streak = 1,
                time = "20:00 - 20:15"
            ),
            Habit(
                userId = userId,
                name = "Learn something new",
                description = "Spend time learning a new skill or topic",
                frequency = listOf("Daily"),
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                categoryId = "learning",
                completedDates = emptyList(),
                streak = 0,
                time = "19:00 - 20:00"
            )
        )
    }

    /**
     * Seed all sample data (user + habits)
     */
    suspend fun seedAllSampleData(): Boolean {
        val userSuccess = seedSampleUserProfile()
        val habitsSuccess = seedSampleHabits()
        return userSuccess && habitsSuccess
    }

    /**
     * Clear all user data (for testing purposes)
     */
    suspend fun clearUserData(): Boolean {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return false

            // Get all user habits and delete them
            val habits = habitRepository.getHabitsForUser(currentUserId)
            var allSuccess = true
            habits.forEach { habit ->
                val success = habitRepository.deleteHabit(habit.id)
                if (!success) allSuccess = false
            }

            println("Clear user data completed. Success: $allSuccess")
            allSuccess
        } catch (e: Exception) {
            println("Error clearing user data: ${e.message}")
            false
        }
    }
}
