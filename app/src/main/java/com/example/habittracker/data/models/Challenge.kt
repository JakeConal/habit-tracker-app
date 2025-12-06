package com.example.habittracker.data.models

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imgURL: String = "",
    val duration: ChallengeDuration = ChallengeDuration.SEVEN_DAYS,
    val reward: Int = 0,
    val isJoined: Boolean = false,
)

enum class ChallengeDuration(val duration: String, val color: BadgeColor) {
    SEVEN_DAYS("7 Days Challenge", BadgeColor.CYAN),
    THIRTY_DAYS("30 Days Challenge", BadgeColor.GREEN),
}

enum class BadgeColor {
    CYAN,
    GREEN,
}