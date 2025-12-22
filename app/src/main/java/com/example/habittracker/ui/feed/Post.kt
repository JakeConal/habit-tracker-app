package com.example.habittracker.ui.feed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: String,
    val userId: String,
    val authorName: String,
    val authorAvatar: String,
    val timestamp: String,
    val content: String,
    val imageUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val comments: List<Comment> = emptyList()
) : Parcelable

