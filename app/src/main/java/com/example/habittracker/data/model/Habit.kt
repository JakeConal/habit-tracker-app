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
    val quantity: Int = 0,
    val unit: String = "",
    val frequency: List<String> = listOf("Daily"), // List of days or frequencies like ["Monday", "Wednesday"] or ["Daily"]
    val createdAt: Long = System.currentTimeMillis(),
    val categoryId: String = "", // ID of the category this habit belongs to
    val completedDates: List<String> = emptyList(), // Track completed dates
    val streak: Int = 0, // Current streak count
    val isPomodoroRequired: Boolean = false,
    val focusDuration: Int = 25, // in minutes
    val shortBreak: Int = 5, // in minutes
    val longBreak: Int = 15, // in minutes
    val totalSessions: Int = 4,
    val isChallengeHabit: Boolean = false,
    val challengeId: String? = null,
    val challengeImageUrl: String? = null,
    val challengeDescription: String? = null,
    val challengeDurationDays: Int? = null
) {
    companion object {
        const val COLLECTION_NAME = "habits"

        // Convert Firestore DocumentSnapshot to Habit object
        @Suppress("UNCHECKED_CAST")
        fun fromDocument(document: DocumentSnapshot): Habit? {
            return try {
                Habit(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    name = document.getString("name") ?: "",
                    quantity = document.getLong("quantity")?.toInt() ?: 0,
                    unit = document.getString("unit") ?: "",
                    frequency = document.get("frequency") as? List<String> ?: listOf("Daily"),
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    categoryId = document.getString("categoryId") ?: "",
                    completedDates = document.get("completedDates") as? List<String> ?: emptyList(),
                    streak = document.getLong("streak")?.toInt() ?: 0,
                    isPomodoroRequired = document.getBoolean("isPomodoroRequired") ?: false,
                    focusDuration = document.getLong("focusDuration")?.toInt() ?: 25,
                    shortBreak = document.getLong("shortBreak")?.toInt() ?: 5,
                    longBreak = document.getLong("longBreak")?.toInt() ?: 15,
                    totalSessions = document.getLong("totalSessions")?.toInt() ?: 4,
                    isChallengeHabit = document.getBoolean("isChallengeHabit") ?: false,
                    challengeId = document.getString("challengeId"),
                    challengeImageUrl = document.getString("challengeImageUrl"),
                    challengeDescription = document.getString("challengeDescription"),
                    challengeDurationDays = document.getLong("challengeDurationDays")?.toInt()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert Habit object to Map for Firestore
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "name" to name,
            "quantity" to quantity,
            "unit" to unit,
            "frequency" to frequency,
            "createdAt" to createdAt,
            "categoryId" to categoryId,
            "completedDates" to completedDates,
            "streak" to streak,
            "isPomodoroRequired" to isPomodoroRequired,
            "focusDuration" to focusDuration,
            "shortBreak" to shortBreak,
            "longBreak" to longBreak,
            "totalSessions" to totalSessions,
            "isChallengeHabit" to isChallengeHabit
        )

        challengeId?.let { map["challengeId"] = it }
        challengeImageUrl?.let { map["challengeImageUrl"] = it }
        challengeDescription?.let { map["challengeDescription"] = it }
        challengeDurationDays?.let { map["challengeDurationDays"] = it }

        return map
    }
}
