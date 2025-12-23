package com.example.habittracker.data.model

data class User(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val points: Int = 0,
    val rank: Int = 0
)