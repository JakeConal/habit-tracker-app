package com.example.habittracker.data.model

data class Post(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String?, // Can be null if using local placeholder
    val content: String,
    val timestamp: Long,
    val likeCount: Int,
    val commentCount: Int,
    val imageUrl: String? = null
)

