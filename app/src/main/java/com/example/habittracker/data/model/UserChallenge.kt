package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Model đại diện cho mối quan hệ giữa User và Challenge
 * Lưu thông tin về việc user tham gia challenge nào
 * Tránh lặp dữ liệu bằng cách không lưu isJoined trong Challenge model
 */
data class UserChallenge(
    val id: String = "", // Document ID: "userId_challengeId"
    val userId: String = "",
    val challengeId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null, // Null nếu chưa hoàn thành
    val progress: Int = 0, // Phần trăm hoàn thành (0-100)
    val status: UserChallengeStatus = UserChallengeStatus.ONGOING // ONGOING, COMPLETED, ABANDONED
) {
    companion object {
        const val COLLECTION_NAME = "userChallenges"

        // Convert Firestore DocumentSnapshot to UserChallenge object
        fun fromDocument(document: DocumentSnapshot): UserChallenge? {
            return try {
                UserChallenge(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    challengeId = document.getString("challengeId") ?: "",
                    joinedAt = document.getLong("joinedAt") ?: System.currentTimeMillis(),
                    completedAt = document.getLong("completedAt"),
                    progress = document.getLong("progress")?.toInt() ?: 0,
                    status = UserChallengeStatus.valueOf(document.getString("status") ?: "ONGOING")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert UserChallenge object to Map for Firestore
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "challengeId" to challengeId,
            "joinedAt" to joinedAt,
            "progress" to progress,
            "status" to status.name
        )
        if (completedAt != null) {
            map["completedAt"] = completedAt
        }
        return map
    }

    // Tạo ID duy nhất cho UserChallenge
    fun generateId(): String {
        return "${userId}_${challengeId}"
    }
}

enum class UserChallengeStatus {
    ONGOING,      // Đang thực hiện
    COMPLETED,    // Đã hoàn thành
    ABANDONED     // Đã bỏ cuộc
}
