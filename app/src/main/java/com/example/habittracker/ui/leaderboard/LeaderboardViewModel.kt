package com.example.habittracker.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LeaderboardViewModel : ViewModel() {
    private val userRepository = FirestoreUserRepository.getInstance()
    private val PAGE_SIZE = 20L

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)

    val topUsers: StateFlow<List<User>> = _currentLimit
        .flatMapLatest { limit ->
            userRepository.listenToTopUsers(limit)
                .onStart { _isLoading.value = true }
                .onEach { _isLoading.value = false }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val currentUserRankInfo: StateFlow<Pair<User?, Int>> = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
        userRepository.listenToUser(userId)
            .flatMapLatest { user ->
                if (user != null) {
                    userRepository.listenToUserRank(user.points).map { rank ->
                        Pair(user, rank)
                    }
                } else {
                    kotlinx.coroutines.flow.flowOf(Pair(null, 0))
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Pair(null, 0))
    } ?: MutableStateFlow<Pair<User?, Int>>(Pair(null, 0)).asStateFlow()


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    fun loadTopUsers(isRefresh: Boolean = true) {
        if (_isLoading.value && !isRefresh) return

        if (isRefresh) {
            _hasMoreData = true
            // If already at first page, trigger a manual "finish loading" to stop spinner
            // since Flow might not emit if limit is unchanged
            if (_currentLimit.value == PAGE_SIZE) {
                _isLoading.value = true
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = true
                _currentLimit.value = PAGE_SIZE
            }
            return
        }

        if (!_hasMoreData) return


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