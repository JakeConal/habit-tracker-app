package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val points: Int = 0,
    val rank: Int = 0,
    val email: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "users"

        // Convert Firestore DocumentSnapshot to User object
        fun fromDocument(document: DocumentSnapshot): User? {
            return try {
                User(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    avatarUrl = document.getString("avatarUrl"),
                    points = document.getLong("points")?.toInt() ?: 0,
                    rank = document.getLong("rank")?.toInt() ?: 0,
                    email = document.getString("email"),
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastLoginAt = document.getLong("lastLoginAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert User object to Map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "avatarUrl" to (avatarUrl ?: ""),
            "points" to points,
            "rank" to rank,
            "email" to (email ?: ""),
            "createdAt" to createdAt,
            "lastLoginAt" to lastLoginAt
        )
    }
}
