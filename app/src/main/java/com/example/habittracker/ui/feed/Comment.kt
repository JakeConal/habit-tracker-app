package com.example.habittracker.ui.feed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val timestamp: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<CommentReply> = emptyList()
) : Parcelable

@Parcelize
data class CommentReply(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val timestamp: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<CommentReply> = emptyList()
) : Parcelable

