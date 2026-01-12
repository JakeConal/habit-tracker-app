package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Represents a friend relationship in Firestore (users/{userId}/friends/{friendId})
 */
data class Friend(
    val userId: String = "", // The friend's user ID
    val since: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Friend? {
            return try {
                Friend(
                    userId = doc.id,
                    since = doc.getLong("since") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Represents a friend request in Firestore
 */
data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarUrl: String = "",
    val receiverId: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "friend_requests"
        
        fun fromDocument(doc: DocumentSnapshot): FriendRequest? {
            return try {
                FriendRequest(
                    id = doc.id,
                    senderId = doc.getString("senderId") ?: "",
                    senderName = doc.getString("senderName") ?: "",
                    senderAvatarUrl = doc.getString("senderAvatarUrl") ?: "",
                    receiverId = doc.getString("receiverId") ?: "",
                    status = doc.getString("status") ?: "pending",
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "senderAvatarUrl" to senderAvatarUrl,
            "receiverId" to receiverId,
            "status" to status,
            "timestamp" to timestamp
        )
    }
}
