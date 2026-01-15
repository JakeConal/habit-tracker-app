package com.example.habittracker.ui.setting.subsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewChallengeViewModel : ViewModel() {
    private val repository = ChallengeRepository()

    private val _pendingChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val pendingChallenges: StateFlow<List<Challenge>> = _pendingChallenges.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadPendingChallenges() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val challenges = repository.getPendingChallenges()
                _pendingChallenges.value = challenges
            } catch (e: Exception) {
                _message.value = "Failed to load challenges: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val success = repository.approveChallenge(challenge.id)
            if (success) {
                _message.value = "Challenge approved!"
                loadPendingChallenges()
            } else {
                _message.value = "Failed to approve challenge"
            }
        }
    }

    fun rejectChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val success = repository.rejectChallenge(challenge.id)
            if (success) {
                _message.value = "Challenge rejected!"
                loadPendingChallenges()
            } else {
                _message.value = "Failed to reject challenge"
            }
        }
    }
}
