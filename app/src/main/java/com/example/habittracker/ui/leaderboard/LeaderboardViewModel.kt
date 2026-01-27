package com.example.habittracker.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val userRepository = FirestoreUserRepository.getInstance()
    private val PAGE_SIZE = 20L

    private val _topUsers = MutableStateFlow<List<User>>(emptyList())
    val topUsers: StateFlow<List<User>> = _topUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    init {
        loadTopUsers(isRefresh = true)
    }

    fun loadTopUsers(isRefresh: Boolean = true) {
        if (_isLoading.value || (!isRefresh && !_hasMoreData)) return

        if (isRefresh) {
            lastDocument = null
            _hasMoreData = true
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = userRepository.getTopUsersPaginated(PAGE_SIZE, lastDocument)

                result.onSuccess { (newUsers, lastSnapshot) ->
                    if (isRefresh) {
                        _topUsers.value = newUsers
                    } else {
                        _topUsers.value = _topUsers.value + newUsers
                    }

                    lastDocument = lastSnapshot
                    _hasMoreData = newUsers.size >= PAGE_SIZE
                }.onFailure { e ->
                    _error.value = "Failed to load leaderboard: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load leaderboard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}