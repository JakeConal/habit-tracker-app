package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val detail: String = "",
    val keyResults: String = "",
    val imgURL: String = "",
    val duration: ChallengeDuration = ChallengeDuration.SEVEN_DAYS,
    val reward: Int = 0,
    val creatorId: String = "", // ID của user tạo challenge
    val createdAt: Long = System.currentTimeMillis(),
    val participantCount: Int = 0, // Số lượng user đã tham gia
) {
    companion object {
        const val COLLECTION_NAME = "challenges"

        // Convert Firestore DocumentSnapshot to Challenge object
        fun fromDocument(document: DocumentSnapshot): Challenge? {
            return try {
                Challenge(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    description = document.getString("description") ?: "",
                    detail = document.getString("detail") ?: "",
                    keyResults = document.getString("keyResults") ?: "",
                    imgURL = document.getString("imgURL") ?: "",
                    duration = ChallengeDuration.valueOf(document.getString("duration") ?: "SEVEN_DAYS"),
                    reward = document.getLong("reward")?.toInt() ?: 0,
                    creatorId = document.getString("creatorId") ?: "",
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    participantCount = document.getLong("participantCount")?.toInt() ?: 0
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert Challenge object to Map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "description" to description,
            "detail" to detail,
            "keyResults" to keyResults,
            "imgURL" to imgURL,
            "duration" to duration.name,
            "reward" to reward,
            "creatorId" to creatorId,
            "createdAt" to createdAt,
            "participantCount" to participantCount
        )
    }
}

enum class ChallengeDuration(val duration: String, val color: BadgeColor) {
    SEVEN_DAYS("7 Days Challenge", BadgeColor.CYAN),
    THIRTY_DAYS("30 Days Challenge", BadgeColor.GREEN),
    HUNDRED_DAYS("100 Days Challenge", BadgeColor.YELLOW),
}

enum class BadgeColor {
    CYAN,
    GREEN,
    YELLOW,
}

