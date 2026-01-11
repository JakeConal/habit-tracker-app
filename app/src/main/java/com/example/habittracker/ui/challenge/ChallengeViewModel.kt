package com.example.habittracker.ui.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeRepository
import kotlinx.coroutines.launch

class ChallengeViewModel : ViewModel() {
    private val challengeRepository = ChallengeRepository()

    private val _challenges = MutableLiveData<List<Challenge>>()
    val challenges: LiveData<List<Challenge>> = _challenges

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadChallenges() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val challenges = challengeRepository.getAllChallenges()
                _challenges.postValue(challenges)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading challenges: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun getChallengeById(id: String) {
        viewModelScope.launch {
            try {
                val challenge = challengeRepository.getChallengeById(id)
                challenge?.let {
                    _challenges.postValue(listOf(it))
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error loading challenge: ${e.message}")
            }
        }
    }
}