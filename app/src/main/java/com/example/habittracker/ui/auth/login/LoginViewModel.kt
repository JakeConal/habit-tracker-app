package com.example.habittracker.ui.auth.login

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Sign in with email and password
     */
    fun signInWithEmail(email: String, password: String) {
        if (!validateEmailPassword(email, password)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithEmail(email, password)
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginState.Success(user)
                },
                onFailure = { exception ->
                    _loginState.value = LoginState.Error(exception.message ?: "Sign in failed")
                }
            )
        }
    }

    /**
     * Handle Google Sign-In credential response
     */
    fun handleGoogleSignIn(result: GetCredentialResponse) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken

                            val authResult = authRepository.signInWithGoogle(idToken)
                            _isLoading.value = false

                            authResult.fold(
                                onSuccess = { user ->
                                    _loginState.value = LoginState.Success(user)
                                },
                                onFailure = { exception ->
                                    _loginState.value = LoginState.Error(exception.message ?: "Google sign in failed")
                                }
                            )
                        } catch (e: Exception) {
                            _isLoading.value = false
                            _loginState.value = LoginState.Error("Failed to process Google credential: ${e.message}")
                        }
                    } else {
                        _isLoading.value = false
                        _loginState.value = LoginState.Error("Unexpected credential type")
                    }
                }
                else -> {
                    _isLoading.value = false
                    _loginState.value = LoginState.Error("Unexpected credential type")
                }
            }
        }
    }

    /**
     * Sign in anonymously (as guest)
     */
    fun signInAsGuest() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInAnonymously()
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginState.Success(user)
                },
                onFailure = { exception ->
                    _loginState.value = LoginState.Error(exception.message ?: "Guest sign in failed")
                }
            )
        }
    }

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _loginState.value = LoginState.Error("Please enter your email address")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.sendPasswordResetEmail(email)
            _isLoading.value = false

            result.fold(
                onSuccess = {
                    _loginState.value = LoginState.PasswordResetSent
                },
                onFailure = { exception ->
                    _loginState.value = LoginState.Error(exception.message ?: "Failed to send reset email")
                }
            )
        }
    }

    /**
     * Validate email and password
     */
    private fun validateEmailPassword(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _loginState.value = LoginState.Error("Email is required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = LoginState.Error("Invalid email format")
            return false
        }

        if (password.isBlank()) {
            _loginState.value = LoginState.Error("Password is required")
            return false
        }

        if (password.length < 6) {
            _loginState.value = LoginState.Error("Password must be at least 6 characters")
            return false
        }

        return true
    }

    /**
     * Reset login state
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Sealed class representing different login states
 */
sealed class LoginState {
    object Idle : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
    object PasswordResetSent : LoginState()
}
