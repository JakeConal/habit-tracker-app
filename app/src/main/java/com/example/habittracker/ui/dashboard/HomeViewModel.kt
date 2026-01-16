package com.example.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * HomeViewModel - ViewModel for Home/Dashboard screen
 * Manages the UI state and business logic for displaying habits
 */
class HomeViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()
    private val habitRepository = HabitRepository.getInstance()
    private val firestoreUserRepository = FirestoreUserRepository.getInstance()
    private val categoryRepository = CategoryRepository.getInstance()

    // UI State - Habits
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    // UI State - Categories
    private val _categories = MutableStateFlow<List<com.example.habittracker.data.model.Category>>(emptyList())
    val categories: StateFlow<List<com.example.habittracker.data.model.Category>> = _categories.asStateFlow()

    // UI State - Current User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    // Current user ID from Firebase Auth
    private val currentUserId: String?
        get() = authRepository.getCurrentUser()?.uid

    init {
        loadUserAndHabits()
    }

    /**
     * Load current user info and their habits from Firestore
     */
    fun loadUserAndHabits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Get current user ID
                val userId = currentUserId
                if (userId != null) {
                    // Load user info from Firestore
                    val user = firestoreUserRepository.getUserById(userId)

                    _currentUser.value = user

                    // Load user's habits from Firestore
                    val habitsList = habitRepository.getHabitsForUser(userId)

                    // Filter out expired challenge habits
                    val currentTime = System.currentTimeMillis()
                    val filteredHabits = habitsList.filter { habit ->
                        if (habit.isChallengeHabit && habit.challengeDurationDays != null) {
                            val durationMillis = habit.challengeDurationDays.toLong() * 24 * 60 * 60 * 1000
                            val expiryTime = habit.createdAt + durationMillis
                            currentTime <= expiryTime
                        } else {
                            true
                        }
                    }
                    _habits.value = filteredHabits

                    // Load user's categories from Firestore
                    val categoriesList = categoryRepository.getCategoriesForUser(userId)
                    _categories.value = categoriesList
                }
            } catch (e: Exception) {
                _error.emit("Error loading user and habits: ${e.message}")
                _habits.value = emptyList()
                _currentUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all habits for current user
     */
    fun loadHabits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = currentUserId
                if (userId != null) {
                    val habitsList = habitRepository.getHabitsForUser(userId)

                    // Filter out expired challenge habits
                    val currentTime = System.currentTimeMillis()
                    val filteredHabits = habitsList.filter { habit ->
                        if (habit.isChallengeHabit && habit.challengeDurationDays != null) {
                            val durationMillis = habit.challengeDurationDays.toLong() * 24 * 60 * 60 * 1000
                            val expiryTime = habit.createdAt + durationMillis
                            currentTime <= expiryTime
                        } else {
                            true
                        }
                    }
                    _habits.value = filteredHabits
                    // Also reload categories
                    val categoriesList = categoryRepository.getCategoriesForUser(userId)
                    _categories.value = categoriesList
                }
            } catch (e: Exception) {
                _error.emit("Error loading habits: ${e.message}")
                _habits.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle habit completion status
     */
    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            try {
                habitRepository.toggleHabitCompletion(habit.id)
                loadHabits() // Reload habits to reflect changes
            } catch (e: Exception) {
                _error.emit("Error updating habit: ${e.message}")
            }
        }
    }

    /**
     * Toggle habit completion for UI Habit (with Int ID)
     * This will find the corresponding Firestore habit and update it
     */
    fun toggleHabitCompletionByUiId(uiHabitId: Int) {
        viewModelScope.launch {
            try {
                // Find habit with matching ID hash
                val habit = _habits.value.find { it.id.hashCode() == uiHabitId }
                if (habit != null) {
                    habitRepository.toggleHabitCompletion(habit.id)
                    loadHabits() // Reload habits to reflect changes
                }
            } catch (e: Exception) {
                _error.emit("Error updating habit: ${e.message}")
            }
        }
    }

    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habitId)
                loadHabits() // Reload habits to reflect changes
            } catch (e: Exception) {
                _error.emit("Error deleting habit: ${e.message}")
            }
        }
    }

    /**
     * Get a habit by ID
     */
    fun getHabitById(habitId: String): Habit? {
        return _habits.value.find { it.id == habitId }
    }

    /**
     * Update user points
     */
    fun updateUserPoints(points: Int) {
        viewModelScope.launch {
            try {
                val userId = currentUserId
                if (userId != null) {
                    firestoreUserRepository.updateUserPoints(userId, points)
                    // Reload user info
                    val updatedUser = firestoreUserRepository.getUserById(userId)
                    _currentUser.value = updatedUser
                }
            } catch (e: Exception) {
                _error.emit("Error updating user points: ${e.message}")
            }
        }
    }
}
