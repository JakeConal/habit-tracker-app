package com.example.habittracker.ui.auth.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()

    private val _resetState = MutableLiveData<ForgotPasswordState>()
    val resetState: LiveData<ForgotPasswordState> = _resetState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        if (!validateEmail(email)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.sendPasswordResetEmail(email)
            _isLoading.value = false

            result.fold(
                onSuccess = {
                    _resetState.value = ForgotPasswordState.Success
                },
                onFailure = { exception ->
                    _resetState.value = ForgotPasswordState.Error(
                        exception.message ?: "Failed to send reset email"
                    )
                }
            )
        }
    }

    /**
     * Validate email format
     */
    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            _resetState.value = ForgotPasswordState.Error("Email is required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetState.value = ForgotPasswordState.Error("Invalid email format")
            return false
        }

        return true
    }

    /**
     * Reset state
     */
    fun resetState() {
        _resetState.value = ForgotPasswordState.Idle
    }
}

/**
 * Sealed class representing different forgot password states
 */
sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
