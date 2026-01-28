package com.example.habittracker.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.data.repository.ChallengeWithStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {
    private val challengeRepository = ChallengeRepository()
    private val userChallengeRepository = com.example.habittracker.data.repository.UserChallengeRepository()
    private val PAGE_SIZE = 10L

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val challengesWithStatus: StateFlow<List<ChallengeWithStatus>> = _currentLimit
        .flatMapLatest { limit ->
            challengeRepository.listenToVisibleChallenges(userId, limit)
        }
        .map { challenges ->
            val result = mutableListOf<ChallengeWithStatus>()
            for (challenge in challenges) {
                val isJoined = if (userId.isNotEmpty()) {
                    userChallengeRepository.hasUserJoinedChallenge(userId, challenge.id)
                } else false
                result.add(ChallengeWithStatus(challenge, isJoined))
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    fun loadChallenges(isRefresh: Boolean = true) {
        if (_isLoading.value) return

        if (isRefresh) {
            _currentLimit.value = PAGE_SIZE
            _hasMoreData = true
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

    fun getChallengeById(id: String) {
        viewModelScope.launch {
            try {
                val challenge = challengeRepository.getChallengeById(id)
                // This method seems unused in the current Fragment setup, but fixing it anyway
                if (challenge != null) {
                    // We don't have _challengesWithStatus anymore, so we might want to log or handle differently
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading challenge: ${e.message}"
            }
        }
    }
}