package com.example.habittracker.data.models

data class Comment(
    val id: String,
    val author: Author,
    val content: String,
    val timestamp: Long
)

