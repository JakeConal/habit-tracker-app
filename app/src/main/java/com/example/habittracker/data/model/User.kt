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
    val lastLoginAt: Long = System.currentTimeMillis(),
    val joinedChallengeIds: List<String> = emptyList(), // Danh sách ID của challenge đã tham gia
    val notificationsEnabled: Boolean = true, // Whether the user wants to receive notifications
    val fcmToken: String? = null
) {
    companion object {
        const val COLLECTION_NAME = "users"

        // Convert Firestore DocumentSnapshot to User object
        fun fromDocument(document: DocumentSnapshot): User? {
            return try {
                val id = document.id
                val name = document.getString("name") ?: ""
                val avatarUrl = document.getString("avatarUrl")
                val points = document.getLong("points")?.toInt() ?: 0
                val rank = document.getLong("rank")?.toInt() ?: 0
                val email = document.getString("email")
                val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                val lastLoginAt = document.getLong("lastLoginAt") ?: System.currentTimeMillis()
                val joinedChallengeIds = (document.get("joinedChallengeIds") as? List<String>) ?: emptyList()
                val notificationsEnabled = document.getBoolean("notificationsEnabled") ?: true
                val fcmToken = document.getString("fcmToken")

                User(
                    id = id,
                    name = name,
                    avatarUrl = avatarUrl,
                    points = points,
                    rank = rank,
                    email = email,
                    createdAt = createdAt,
                    lastLoginAt = lastLoginAt,
                    joinedChallengeIds = joinedChallengeIds,
                    notificationsEnabled = notificationsEnabled,
                    fcmToken = fcmToken
                )
            } catch (e: Exception) {
                e.printStackTrace()
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
            "fcmToken" to (fcmToken ?: ""),
            "lastLoginAt" to lastLoginAt,
            "joinedChallengeIds" to joinedChallengeIds,
            "notificationsEnabled" to notificationsEnabled
        )
    }
}
