package com.example.habittracker.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val imageUrl: String? = null,
    val hiddenBy: List<String> = emptyList(),
    val likedBy: List<String> = emptyList()
) : Parcelable {
    companion object {
        const val COLLECTION_NAME = "posts"

        // Convert Firestore DocumentSnapshot to Post object
        fun fromDocument(document: DocumentSnapshot): Post? {
            return try {
                Post(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    authorName = document.getString("authorName") ?: "",
                    authorAvatarUrl = document.getString("authorAvatarUrl"),
                    content = document.getString("content") ?: "",
                    timestamp = document.getLong("timestamp") ?: System.currentTimeMillis(),
                    likeCount = document.getLong("likeCount")?.toInt() ?: 0,
                    commentCount = document.getLong("commentCount")?.toInt() ?: 0,
                    shareCount = document.getLong("shareCount")?.toInt() ?: 0,
                    imageUrl = document.getString("imageUrl"),
                    hiddenBy = (document.get("hiddenBy") as? List<String>) ?: emptyList(),
                    likedBy = (document.get("likedBy") as? List<String>) ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Convert Post object to Map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "authorName" to authorName,
            "authorAvatarUrl" to authorAvatarUrl,
            "content" to content,
            "timestamp" to timestamp,
            "likeCount" to likeCount,
            "commentCount" to commentCount,
            "shareCount" to shareCount,
            "imageUrl" to imageUrl,
            "hiddenBy" to hiddenBy,
            "likedBy" to likedBy
        )
    }
}
