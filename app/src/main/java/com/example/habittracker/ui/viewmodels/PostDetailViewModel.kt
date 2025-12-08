package com.example.habittracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.habittracker.data.models.Author
import com.example.habittracker.data.models.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PostDetailViewModel : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun loadComments(postId: String) {
        // In a real app, you would fetch comments for the given postId
        _comments.value = listOf(
            Comment("1", Author("user2", "Michael Chen", ""), "Great job!", System.currentTimeMillis()),
            Comment("2", Author("user3", "Emily Davis", ""), "Keep it up!", System.currentTimeMillis())
        )
    }

    fun addComment(postId: String, content: String) {
        val newComment = Comment(
            (Math.random() * 1000).toString(),
            Author("user1", "You", ""),
            content,
            System.currentTimeMillis()
        )
        _comments.value = _comments.value + newComment
    }
}

