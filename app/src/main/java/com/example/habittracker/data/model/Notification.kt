package com.example.habittracker.data.model

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarUrl: String = "",
    val postId: String = "",
    val commentId: String? = null,
    val type: NotificationType = NotificationType.LIKE_POST,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
) {
    enum class NotificationType {
        LIKE_POST,
        COMMENT_POST,
        SHARE_POST,
        REPLY_COMMENT,
        LIKE_COMMENT,
        DISLIKE_COMMENT
    }
}

