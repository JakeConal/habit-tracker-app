package com.example.habittracker.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.ChallengeStatus
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.data.repository.ChallengeWithStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {
    private val challengeRepository = ChallengeRepository()
    private val PAGE_SIZE = 10L

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val challengesWithStatus: StateFlow<List<ChallengeWithStatus>> = _currentLimit
        .flatMapLatest { limit ->
            // Challenges Flow
            val challengesFlow = challengeRepository.listenToVisibleChallenges(userId, limit)
                .onStart { _isLoading.value = true }
                .onEach { _isLoading.value = false }

            // Joined status Flow - efficiently using the user's joinedChallengeIds field
            val joinedIdsFlow = com.example.habittracker.data.repository.FirestoreUserRepository.getInstance()
                .listenToUser(userId)
                .map { it?.joinedChallengeIds ?: emptyList() }

            challengesFlow.combine(joinedIdsFlow) { challenges, joinedIds ->
                challenges.map { ChallengeWithStatus(it, joinedIds.contains(it.id)) }
                    .sortedBy { if (it.challenge.status == ChallengeStatus.PENDING) 0 else 1 }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    fun loadChallenges(isRefresh: Boolean = true) {
        if (_isLoading.value && !isRefresh) return

        if (isRefresh) {
            _hasMoreData = true
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

        _isLoading.value = true

        val currentCount = challengesWithStatus.value.size
        if (currentCount < _currentLimit.value) {
            _hasMoreData = false
            _isLoading.value = false
            return
        }

        _currentLimit.value += PAGE_SIZE
        _isLoading.value = false
    }

    fun deleteChallenge(challengeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = challengeRepository.deleteChallenge(challengeId)
            _isLoading.value = false
            if (!success) {
                _errorMessage.value = "Failed to delete challenge"
            }
        }
    }
}