package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Data class representing a Habit
 * This is the Model in MVVM architecture
 */
data class Habit(
    val id: String = "", // Changed from Long to String for Firestore compatibility
    val userId: String = "", // User ID who owns this habit
    val name: String = "",
    val description: String = "",
    val frequency: List<String> = listOf("Daily"), // List of days or frequencies like ["Monday", "Wednesday"] or ["Daily"]
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val categoryId: String = "", // ID of the category this habit belongs to
    val completedDates: List<String> = emptyList(), // Track completed dates
    val streak: Int = 0, // Current streak count
    val time: String = "" // Time period for the habit, e.g., "9:00 - 10:00"
) {
    companion object {
        const val COLLECTION_NAME = "habits"

        // Convert Firestore DocumentSnapshot to Habit object
        fun fromDocument(document: DocumentSnapshot): Habit? {
            return try {
                Habit(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                    frequency = document.get("frequency") as? List<String> ?: listOf("Daily"),
                    isCompleted = document.getBoolean("isCompleted") ?: false,
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    categoryId = document.getString("categoryId") ?: "",
                    completedDates = document.get("completedDates") as? List<String> ?: emptyList(),
                    streak = document.getLong("streak")?.toInt() ?: 0,
                    time = document.getString("time") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert Habit object to Map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "description" to description,
            "frequency" to frequency,
            "isCompleted" to isCompleted,
            "createdAt" to createdAt,
            "categoryId" to categoryId,
            "completedDates" to completedDates,
            "streak" to streak,
            "time" to time
        )
    }
}
