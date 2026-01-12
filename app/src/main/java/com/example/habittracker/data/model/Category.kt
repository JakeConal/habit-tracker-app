package com.example.habittracker.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.example.habittracker.R

/**
 * Enum for category icons
 */
enum class CategoryIcon(val resId: Int) {
    HEART(R.drawable.ic_heart),
    BOOK(R.drawable.ic_book),
    MONEY(R.drawable.ic_money),
    BRIEFCASE(R.drawable.ic_briefcase),
    FOOD(R.drawable.ic_food),
    GROWTH(R.drawable.ic_growth),
    MOON(R.drawable.ic_moon),
    FITNESS(R.drawable.ic_fitness),
    HEALTH(R.drawable.ic_health),
    WATER(R.drawable.ic_water),
    WALK(R.drawable.ic_walk),
    MEDITATION(R.drawable.ic_meditation),
    WORK(R.drawable.ic_work),
    OTHER(R.drawable.ic_other)
}

/**
 * Enum for category background colors
 */
enum class CategoryColor(val resId: Int, val colorResId: Int) {
    RED(R.drawable.bg_category_icon_red, R.color.icon_bg_red),
    BLUE(R.drawable.bg_category_icon_blue, R.color.icon_bg_blue),
    YELLOW(R.drawable.bg_category_icon_yellow, R.color.icon_bg_yellow),
    PINK_LIGHT(R.drawable.bg_category_icon_pink_light, R.color.icon_bg_pink_light),
    PURPLE(R.drawable.bg_category_icon_purple, R.color.icon_bg_purple),
    ORANGE_LIGHT(R.drawable.bg_category_icon_orange_light, R.color.icon_bg_orange_light),
    GREEN(R.drawable.bg_category_icon_green, R.color.icon_bg_green),
    INDIGO(R.drawable.bg_category_icon_indigo, R.color.icon_bg_indigo)
}

/**
 * Data class representing a Category
 * Categories define groups for habits with title, color, and icon
 */
data class Category(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val icon: CategoryIcon = CategoryIcon.HEART,
    val color: CategoryColor = CategoryColor.RED,
    val habitCount: Int = 0 // Number of habits in this category
) {
    companion object {
        const val COLLECTION_NAME = "categories"

        // Convert Firestore DocumentSnapshot to Category object
        fun fromDocument(document: DocumentSnapshot): Category? {
            return try {
                Category(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    title = document.getString("title") ?: "",
                    color = CategoryColor.entries.firstOrNull { it.resId == document.getLong("color")?.toInt() } ?: CategoryColor.RED,
                    icon = CategoryIcon.entries.firstOrNull { it.resId == document.getLong("icon")?.toInt() } ?: CategoryIcon.HEART
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    // Convert Category object to Map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "color" to color.resId,
            "icon" to icon.resId
        )
    }
}
