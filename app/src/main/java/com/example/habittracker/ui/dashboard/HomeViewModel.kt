package com.example.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.data.repository.ChallengeRepository
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
    private val challengeRepository = ChallengeRepository()

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

                    // Check and reward ended challenges
                    checkAndRewardChallenges(habitsList, user)

                    // Refresh habits list after rewarding (habits might have been updated)
                    val updatedHabitsList = if (habitsList.any { it.isChallengeHabit && !it.isChallengeRewarded })
                        habitRepository.getHabitsForUser(userId)
                    else
                        habitsList

                    // Filter out expired challenge habits
                    val currentTime = System.currentTimeMillis()
                    val filteredHabits = updatedHabitsList.filter { habit ->
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
                val user = _currentUser.value ?: (if (userId != null) firestoreUserRepository.getUserById(userId) else null)

                if (userId != null) {
                    val habitsList = habitRepository.getHabitsForUser(userId)

                    // Check and reward ended challenges
                    if (user != null) {
                        checkAndRewardChallenges(habitsList, user)
                    }

                    // Refresh habits list after rewarding
                    val updatedHabitsList = if (habitsList.any { it.isChallengeHabit && !it.isChallengeRewarded })
                        habitRepository.getHabitsForUser(userId)
                    else
                        habitsList

                    // Filter out expired challenge habits
                    val currentTime = System.currentTimeMillis()
                    val filteredHabits = updatedHabitsList.filter { habit ->
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
     * Check and reward ended challenge habits
     */
    private suspend fun checkAndRewardChallenges(habitsList: List<Habit>, user: User?) {
        if (user == null) return

        val currentTime = System.currentTimeMillis()
        var totalPointsToAdd = 0
        val habitsToUpdate = mutableListOf<Habit>()

        for (habit in habitsList) {
            if (habit.isChallengeHabit && !habit.isChallengeRewarded && habit.challengeDurationDays != null) {
                val durationMillis = habit.challengeDurationDays.toLong() * 24 * 60 * 60 * 1000
                val expiryTime = habit.createdAt + durationMillis

                if (currentTime > expiryTime) {
                    // Challenge has ended
                    val totalDays = habit.challengeDurationDays
                    val completedDays = habit.completedDates.size
                    val percentage = (completedDays.toDouble() / totalDays.toDouble()) * 100

                    // Fetch challenge to get reward points
                    val rewardPoints = habit.challengeId?.let { chId ->
                        challengeRepository.getChallengeById(chId)?.reward
                    } ?: 0

                    val pointsToAdd = if (rewardPoints > 0) {
                        ((percentage / 100.0) * rewardPoints).toInt()
                    } else {
                        percentage.toInt() // Fallback to percentage number as points
                    }

                    totalPointsToAdd += pointsToAdd
                    habitsToUpdate.add(habit.copy(isChallengeRewarded = true))
                }
            }
        }

        if (habitsToUpdate.isNotEmpty()) {
            // Update habits in Firestore
            for (updatedHabit in habitsToUpdate) {
                habitRepository.updateHabit(updatedHabit)
            }

            // Update user points
            if (totalPointsToAdd > 0) {
                val newTotalPoints = user.points + totalPointsToAdd
                firestoreUserRepository.updateUserPoints(user.id, newTotalPoints)

                // Refresh local user state
                val updatedUser = firestoreUserRepository.getUserById(user.id)
                _currentUser.value = updatedUser

                _error.emit("Congratulations! You earned $totalPointsToAdd points from completed challenges!")
            }
        }
    }

    /**
     * Update user points
     */
    fun updateUserPoints(points: Int) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value
                val userId = user?.id ?: currentUserId
                if (userId != null) {
                    val currentPoints = user?.points ?: 0
                    firestoreUserRepository.updateUserPoints(userId, currentPoints + points)
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
