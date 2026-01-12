package com.example.habittracker.ui.setting.subsettings

import android.content.Context
import android.net.Uri

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.utils.ImageUploadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
    object Idle : EditProfileUiState()
}

class EditProfileViewModel : ViewModel() {

    private val userRepository = FirestoreUserRepository.getInstance()
    
    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            val user = userRepository.getCurrentUser()
            _currentUser.value = user
            if (user != null) {
                _uiState.value = EditProfileUiState.Idle
            } else {
                _uiState.value = EditProfileUiState.Error("User not found")
            }
        }
    }

    fun updateProfile(name: String, imageUri: Uri?, context: Context) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            
            try {
                var avatarUrl = user.avatarUrl
                
                // Upload new avatar if selected
                // Upload new avatar if selected
                if (imageUri != null) {
                    val oldAvatarUrl = user.avatarUrl
                    // If user has an old avatar, we might want to delete it or just overwrite/ignore.
                    // ImageUploadHelper.updateUserAvatar handles deletion of old image if provided.
                    avatarUrl = ImageUploadHelper.updateUserAvatar(oldAvatarUrl, imageUri, context)
                }

                val updatedUser = user.copy(
                    name = name,
                    avatarUrl = avatarUrl
                )

                val success = userRepository.updateUserProfile(user.id, name, avatarUrl)
                if (success) {
                    _currentUser.value = updatedUser
                    _uiState.value = EditProfileUiState.Success
                } else {
                    _uiState.value = EditProfileUiState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = EditProfileUiState.Idle
    }
}
