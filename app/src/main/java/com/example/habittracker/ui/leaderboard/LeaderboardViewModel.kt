package com.example.habittracker.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class LeaderboardViewModel : ViewModel() {
    private val userRepository = FirestoreUserRepository.getInstance()
    private val PAGE_SIZE = 20L

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)

    val topUsers: StateFlow<List<User>> = _currentLimit
        .flatMapLatest { limit ->
            userRepository.listenToTopUsers(limit)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    fun loadTopUsers(isRefresh: Boolean = true) {
        if (_isLoading.value) return

        if (isRefresh) {
            _currentLimit.value = PAGE_SIZE
            _hasMoreData = true
            return
        }

        if (!_hasMoreData) return

        _isLoading.value = true

        val currentCount = topUsers.value.size
        if (currentCount < _currentLimit.value) {
            _hasMoreData = false
            _isLoading.value = false
            return
        }

        _currentLimit.value += PAGE_SIZE
        _isLoading.value = false
    }
}