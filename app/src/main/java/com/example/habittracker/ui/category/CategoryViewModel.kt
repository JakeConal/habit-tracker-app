package com.example.habittracker.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CategoryViewModel - ViewModel for Category management screen
 * Manages the UI state and business logic for managing categories
 */
class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository.getInstance()
    private val authRepository = AuthRepository.getInstance()

    // UI State - Categories list
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    // Success events
    private val _categoryAdded = MutableSharedFlow<Boolean>()
    val categoryAdded: SharedFlow<Boolean> = _categoryAdded.asSharedFlow()

    private val _categoryUpdated = MutableSharedFlow<Boolean>()
    val categoryUpdated: SharedFlow<Boolean> = _categoryUpdated.asSharedFlow()

    private val _categoryDeleted = MutableSharedFlow<Boolean>()
    val categoryDeleted: SharedFlow<Boolean> = _categoryDeleted.asSharedFlow()

    // Current user ID
    private val currentUserId: String?
        get() = authRepository.getCurrentUser()?.uid

    init {
        loadCategories()
    }

    /**
     * Load all categories for the current user
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = currentUserId
                if (userId != null) {
                    val categoriesList = repository.getCategoriesForUser(userId)
                    _categories.value = categoriesList
                } else {
                    _error.emit("User not authenticated")
                    _categories.value = emptyList()
                }
            } catch (e: Exception) {
                _error.emit("Failed to load categories: ${e.message}")
                _categories.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new category
     */
    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val categoryId = repository.addCategory(category)
                if (categoryId != null) {
                    _categoryAdded.emit(true)
                    loadCategories() // Reload to get updated list
                } else {
                    _error.emit("Failed to add category")
                }
            } catch (e: Exception) {
                _error.emit("Error adding category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing category
     */
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.updateCategory(category)
                if (success) {
                    _categoryUpdated.emit(true)
                    loadCategories() // Reload to get updated list
                } else {
                    _error.emit("Failed to update category")
                }
            } catch (e: Exception) {
                _error.emit("Error updating category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a category
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.deleteCategory(categoryId)
                if (success) {
                    _categoryDeleted.emit(true)
                    loadCategories() // Reload to get updated list
                } else {
                    _error.emit("Failed to delete category")
                }
            } catch (e: Exception) {
                _error.emit("Error deleting category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get category by ID
     */
    fun getCategoryById(categoryId: String): Category? {
        return _categories.value.find { it.id == categoryId }
    }
}
