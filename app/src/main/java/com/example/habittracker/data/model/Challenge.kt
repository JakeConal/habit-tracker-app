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
    val status: ChallengeStatus = ChallengeStatus.PENDING,
    val votes: Int = 0, // Số lượng vote cho challenge
    val votedBy: List<String> = emptyList() // List IDs of users who voted
) {
    companion object {
        const val COLLECTION_NAME = "challenges"

        // Convert Firestore DocumentSnapshot to Challenge object
        fun fromDocument(document: DocumentSnapshot): Challenge? {
            return try {
                val durationStr = document.getString("duration") ?: "SEVEN_DAYS"
                val duration = try {
                    ChallengeDuration.valueOf(durationStr)
                } catch (e: Exception) {
                    // Fallback for legacy data or mismatched names
                    when (durationStr) {
                        "7 Days Challenge" -> ChallengeDuration.SEVEN_DAYS
                        "30 Days Challenge" -> ChallengeDuration.THIRTY_DAYS
                        "100 Days Challenge" -> ChallengeDuration.HUNDRED_DAYS
                        else -> ChallengeDuration.SEVEN_DAYS
                    }
                }

                Challenge(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    description = document.getString("description") ?: "",
                    detail = document.getString("detail") ?: "",
                    keyResults = document.getString("keyResults") ?: "",
                    imgURL = document.getString("imgURL") ?: "",
                    duration = duration,
                    reward = document.getLong("reward")?.toInt() ?: 0,
                    creatorId = document.getString("creatorId") ?: "",
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    participantCount = document.getLong("participantCount")?.toInt() ?: 0,
                    status = try {
                        ChallengeStatus.valueOf(document.getString("status") ?: "PENDING")
                    } catch (e: Exception) {
                        ChallengeStatus.PENDING
                    },
                    votes = document.getLong("votes")?.toInt() ?: 0,
                    votedBy = (document.get("votedBy") as? List<String>) ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert Challenge object to Map for Firestore
    fun toMap(): Map<String, Any?> {
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
            "participantCount" to participantCount,
            "status" to status.name,
            "votes" to votes,
            "votedBy" to votedBy
        )
    }
}

enum class ChallengeStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class ChallengeDuration(val duration: String, val color: BadgeColor, val days: Int) {
    SEVEN_DAYS("7 Days Challenge", BadgeColor.CYAN, 7),
    THIRTY_DAYS("30 Days Challenge", BadgeColor.GREEN, 30),
    HUNDRED_DAYS("100 Days Challenge", BadgeColor.YELLOW, 100),
}

enum class BadgeColor {
    CYAN,
    GREEN,
    YELLOW,
}
