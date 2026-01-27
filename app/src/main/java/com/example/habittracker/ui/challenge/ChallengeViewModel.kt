package com.example.habittracker.ui.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.data.repository.ChallengeWithStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {
    private val challengeRepository = ChallengeRepository()
    private val PAGE_SIZE = 10L

    private val _challengesWithStatus = MutableLiveData<List<ChallengeWithStatus>>()
    val challengesWithStatus: LiveData<List<ChallengeWithStatus>> = _challengesWithStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var lastDocument: DocumentSnapshot? = null
    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    fun loadChallenges(isRefresh: Boolean = true) {
        if (_isLoading.value == true || (!isRefresh && !_hasMoreData)) return

        if (isRefresh) {
            lastDocument = null
            _hasMoreData = true
        }

        _isLoading.value = true
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        viewModelScope.launch {
            try {
                val result = challengeRepository.getVisibleChallengesPaginated(userId, PAGE_SIZE, lastDocument)

                result.onSuccess { (newChallenges, lastSnapshot, hasMore) ->
                    // Map to ChallengeWithStatus correctly handling suspension
                    val userChallengeRepository = com.example.habittracker.data.repository.UserChallengeRepository()
                    val newChallengesWithStatus = mutableListOf<ChallengeWithStatus>()

                    for (challenge in newChallenges) {
                        val isJoined = if (userId.isNotEmpty()) {
                            userChallengeRepository.hasUserJoinedChallenge(userId, challenge.id)
                        } else false
                        newChallengesWithStatus.add(ChallengeWithStatus(challenge, isJoined))
                    }

                    if (isRefresh) {
                        _challengesWithStatus.postValue(newChallengesWithStatus)
                    } else {
                        val currentList = _challengesWithStatus.value ?: emptyList()
                        _challengesWithStatus.postValue(currentList + newChallengesWithStatus)
                    }

                    lastDocument = lastSnapshot
                    _hasMoreData = hasMore
                }.onFailure { e ->
                    _errorMessage.postValue("Error loading challenges: ${e.message}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading challenges: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getChallengeById(id: String) {
        viewModelScope.launch {
            try {
                val challenge = challengeRepository.getChallengeById(id)
                challenge?.let {
                    _challengesWithStatus.postValue(listOf(ChallengeWithStatus(it, false)))
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading challenge: ${e.message}")
            }
        }
    }
}