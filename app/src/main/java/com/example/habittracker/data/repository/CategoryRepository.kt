package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing Category data, combining local caching with Firestore persistence
 */
class CategoryRepository private constructor(
    private val habitRepository: HabitRepository = HabitRepository.getInstance()
) {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: Flow<List<Category>> = _categories.asStateFlow()

    companion object {
        @Volatile
        private var instance: CategoryRepository? = null

        fun getInstance(): CategoryRepository {
            return instance ?: synchronized(this) {
                instance ?: CategoryRepository().also { instance = it }
            }
        }
    }

    /**
     * Get all categories for a specific user
     */
    suspend fun getCategoriesForUser(userId: String): List<Category> {
        return try {
            val categories = FirestoreManager.getCollectionWhere(
                collectionName = Category.COLLECTION_NAME,
                field = "userId",
                value = userId,
                mapper = { document -> Category.fromDocument(document) }
            )
            
            // Calculate habit count for each category
            val categoriesWithCount = categories.map { category ->
                val habitCount = habitRepository.getHabitCountByCategory(userId, category.id)
                category.copy(habitCount = habitCount)
            }
            
            _categories.value = categoriesWithCount
            categoriesWithCount
        } catch (e: Exception) {
            println("Error getting categories for user: ${e.message}")
            emptyList()
        }
    }

    /**
     * Add a new category
     */
    suspend fun addCategory(category: Category): String? {
        return try {
            val categoryId = FirestoreManager.addDocument(
                Category.COLLECTION_NAME,
                category.toMap()
            )
            if (categoryId != null) {
                // Refresh the categories list
                getCategoriesForUser(category.userId)
            }
            categoryId
        } catch (e: Exception) {
            println("Error adding category: ${e.message}")
            null
        }
    }

    /**
     * Update an existing category
     */
    suspend fun updateCategory(category: Category): Boolean {
        return try {
            val success = FirestoreManager.updateDocument(
                Category.COLLECTION_NAME,
                category.id,
                category.toMap()
            )
            if (success) {
                // Refresh the categories list
                getCategoriesForUser(category.userId)
            }
            success
        } catch (e: Exception) {
            println("Error updating category: ${e.message}")
            false
        }
    }

    /**
     * Delete a category
     */
    suspend fun deleteCategory(categoryId: String): Boolean {
        return try {
            FirestoreManager.deleteDocument(Category.COLLECTION_NAME, categoryId)
        } catch (e: Exception) {
            println("Error deleting category: ${e.message}")
            false
        }
    }

    /**
     * Delete all categories for a user
     */
    suspend fun deleteCategoriesForUser(userId: String): Boolean {
        return try {
            val categories = getCategoriesForUser(userId)
            var allSuccess = true
            for (category in categories) {
                if (!deleteCategory(category.id)) {
                    allSuccess = false
                }
            }
            if (allSuccess) {
                _categories.value = emptyList()
            }
            allSuccess
        } catch (e: Exception) {
            println("Error deleting categories for user: ${e.message}")
            false
        }
    }

    /**
     * Get a category by ID
     */
    suspend fun getCategoryById(categoryId: String): Category? {
        return try {
            val document = FirestoreManager.getDocument(Category.COLLECTION_NAME, categoryId)
            document?.let { Category.fromDocument(it) }
        } catch (e: Exception) {
            println("Error getting category by ID: ${e.message}")
            null
        }
    }

    /**
     * Observe categories for a user with real-time habit count updates
     * This will automatically update when habits are added/deleted/updated
     */
    fun observeCategoriesWithHabitCount(userId: String): Flow<List<Category>> {
        return FirestoreManager.observeCollectionWhere(
            collectionName = Category.COLLECTION_NAME,
            field = "userId",
            value = userId,
            mapper = { document -> Category.fromDocument(document) }
        ).map { categories ->
            // Calculate habit count for each category whenever categories change
            categories.map { category ->
                val habitCount = habitRepository.getHabitCountByCategory(userId, category.id)
                category.copy(habitCount = habitCount)
            }.also { categoriesWithCount ->
                _categories.value = categoriesWithCount
            }
        }
    }

    /**
     * Clear all local cache
     */
    fun clearCache() {
        _categories.value = emptyList()
    }
}
