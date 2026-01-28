package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    // Profile fields
    // Stored as ISO date string: yyyy-MM-dd (optional)
    val dateOfBirth: String? = null,
    // Stored as string enum: male/female/other/prefer_not_to_say (optional)
    val gender: String? = null,
    val points: Int = 0,
    val rank: Int = 0,
    val email: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val joinedChallengeIds: List<String> = emptyList(), // Danh sách ID của challenge đã tham gia
    val notificationsEnabled: Boolean = true, // Whether the user wants to receive notifications
    val fcmToken: String? = null,
    val votedChallengeIds: List<String> = emptyList(), // Danh sách ID challenge đã vote
    val role: String = ROLE_USER
) {
    companion object {
        const val COLLECTION_NAME = "users"
        const val ROLE_USER = "user"
        const val ROLE_ADMIN = "admin"

        const val GENDER_MALE = "male"
        const val GENDER_FEMALE = "female"
        const val GENDER_OTHER = "other"
        const val GENDER_PREFER_NOT_TO_SAY = "prefer_not_to_say"

        // Convert Firestore DocumentSnapshot to User object
        fun fromDocument(document: DocumentSnapshot): User? {
            return try {
                val id = document.id
                val name = document.getString("name") ?: ""
                val avatarUrl = document.getString("avatarUrl")
                val dateOfBirth = document.getString("dateOfBirth")
                val gender = document.getString("gender")
                val points = document.getLong("points")?.toInt() ?: 0
                val rank = document.getLong("rank")?.toInt() ?: 0
                val email = document.getString("email")
                val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                val lastLoginAt = document.getLong("lastLoginAt") ?: System.currentTimeMillis()
                val joinedChallengeIds = (document.get("joinedChallengeIds") as? List<String>) ?: emptyList()
                val notificationsEnabled = document.getBoolean("notificationsEnabled") ?: true
                val fcmToken = document.getString("fcmToken")
                val votedChallengeIds = (document.get("votedChallengeIds") as? List<String>) ?: emptyList()
                val role = document.getString("role") ?: ROLE_USER

                User(
                    id = id,
                    name = name,
                    avatarUrl = avatarUrl,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    points = points,
                    rank = rank,
                    email = email,
                    createdAt = createdAt,
                    lastLoginAt = lastLoginAt,
                    joinedChallengeIds = joinedChallengeIds,
                    notificationsEnabled = notificationsEnabled,
                    fcmToken = fcmToken,
                    votedChallengeIds = votedChallengeIds,
                    role = role
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Convert User object to Map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "avatarUrl" to avatarUrl,
            "dateOfBirth" to dateOfBirth,
            "gender" to gender,
            "points" to points,
            "rank" to rank,
            "email" to email,
            "createdAt" to createdAt,
            "lastLoginAt" to lastLoginAt,
            "joinedChallengeIds" to joinedChallengeIds,
            "notificationsEnabled" to notificationsEnabled,
            "fcmToken" to fcmToken,
            "votedChallengeIds" to votedChallengeIds,
            "role" to role
        )
    }
}
