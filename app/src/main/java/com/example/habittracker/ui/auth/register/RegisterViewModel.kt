package com.example.habittracker.ui.auth.register

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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Register with email and password
     */
    fun registerWithEmail(name: String, email: String, password: String, confirmPassword: String) {
        if (!validateRegistration(name, email, password, confirmPassword)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.registerWithEmail(email, password, name)
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _registerState.value = RegisterState.Success(user)
                },
                onFailure = { exception ->
                    _registerState.value = RegisterState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }

    /**
     * Sign up with Google using Credential Manager
     */
    fun signUpWithGoogle(credentialManager: CredentialManager, request: GetCredentialRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = credentialManager.getCredential(
                    context = null as android.content.Context, // Will be passed from Fragment
                    request = request
                )
                handleGoogleSignUp(result)
            } catch (e: Exception) {
                _isLoading.value = false
                _registerState.value = RegisterState.Error("Google sign up failed: ${e.message}")
            }
        }
    }

    /**
     * Handle Google Sign-Up credential response
     */
    suspend fun handleGoogleSignUp(result: GetCredentialResponse) {
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
                                _registerState.value = RegisterState.Success(user)
                            },
                            onFailure = { exception ->
                                _registerState.value = RegisterState.Error(exception.message ?: "Google sign up failed")
                            }
                        )
                    } catch (e: Exception) {
                        _isLoading.value = false
                        _registerState.value = RegisterState.Error("Failed to process Google credential: ${e.message}")
                    }
                } else {
                    _isLoading.value = false
                    _registerState.value = RegisterState.Error("Unexpected credential type")
                }
            }
            else -> {
                _isLoading.value = false
                _registerState.value = RegisterState.Error("Unexpected credential type")
            }
        }
    }

    /**
     * Validate registration inputs
     */
    private fun validateRegistration(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isBlank()) {
            _registerState.value = RegisterState.Error("Name is required")
            return false
        }

        if (name.length < 2) {
            _registerState.value = RegisterState.Error("Name must be at least 2 characters")
            return false
        }

        if (email.isBlank()) {
            _registerState.value = RegisterState.Error("Email is required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = RegisterState.Error("Invalid email format")
            return false
        }

        if (password.isBlank()) {
            _registerState.value = RegisterState.Error("Password is required")
            return false
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters")
            return false
        }

        if (confirmPassword.isBlank()) {
            _registerState.value = RegisterState.Error("Please confirm your password")
            return false
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return false
        }

        return true
    }

    /**
     * Reset register state
     */
    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

/**
 * Sealed class representing different registration states
 */
sealed class RegisterState {
    object Idle : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
