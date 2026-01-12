package com.example.habittracker.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikesCount: Int = 0,
    val dislikedBy: List<String> = emptyList(),
    val replies: List<Comment> = emptyList()
) : Parcelable {
    companion object {
        const val COLLECTION_NAME = "comments"

        @Suppress("UNCHECKED_CAST")
        fun fromDocument(document: DocumentSnapshot): Comment? {
            return try {
                val repliesData = document.get("replies") as? List<Map<String, Any>> ?: emptyList()
                val repliesList = repliesData.mapNotNull { map ->
                    try {
                        Comment(
                            id = map["id"] as? String ?: "",
                            postId = map["postId"] as? String ?: "",
                            userId = map["userId"] as? String ?: "",
                            authorName = map["authorName"] as? String ?: "",
                            authorAvatarUrl = map["authorAvatarUrl"] as? String,
                            content = map["content"] as? String ?: "",
                            timestamp = (map["timestamp"] as? Long) ?: System.currentTimeMillis(),
                            likesCount = (map["likesCount"] as? Number)?.toInt() ?: 0,
                            likedBy = (map["likedBy"] as? List<String>) ?: emptyList(),
                            dislikesCount = (map["dislikesCount"] as? Number)?.toInt() ?: 0,
                            dislikedBy = (map["dislikedBy"] as? List<String>) ?: emptyList(),
                            replies = emptyList() // Avoid deep recursion for now, only 1 level deep usually
                        )
                    } catch (e: Exception) { null }
                }

                Comment(
                    id = document.id,
                    postId = document.getString("postId") ?: "",
                    userId = document.getString("userId") ?: "",
                    authorName = document.getString("authorName") ?: "",
                    authorAvatarUrl = document.getString("authorAvatarUrl"),
                    content = document.getString("content") ?: "",
                    timestamp = document.getLong("timestamp") ?: System.currentTimeMillis(),
                    likesCount = document.getLong("likesCount")?.toInt() ?: 0,
                    likedBy = (document.get("likedBy") as? List<String>) ?: emptyList(),
                    dislikesCount = document.getLong("dislikesCount")?.toInt() ?: 0,
                    dislikedBy = (document.get("dislikedBy") as? List<String>) ?: emptyList(),
                    replies = repliesList
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id, // include id in map for nested saving
            "postId" to postId,
            "userId" to userId,
            "authorName" to authorName,
            "authorAvatarUrl" to authorAvatarUrl,
            "content" to content,
            "timestamp" to timestamp,
            "likesCount" to likesCount,
            "likedBy" to likedBy,
            "dislikesCount" to dislikesCount,
            "dislikedBy" to dislikedBy,
            "replies" to replies.map { it.toMap() }
        )
    }
}
