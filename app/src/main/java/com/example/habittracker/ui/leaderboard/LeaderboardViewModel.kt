package com.example.habittracker.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val userRepository = FirestoreUserRepository.getInstance()

    private val _topUsers = MutableStateFlow<List<User>>(emptyList())
    val topUsers: StateFlow<List<User>> = _topUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTopUsers()
    }

    fun loadTopUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val users = userRepository.getTopUsers()
                _topUsers.value = users
            } catch (e: Exception) {
                _error.value = "Failed to load leaderboard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}