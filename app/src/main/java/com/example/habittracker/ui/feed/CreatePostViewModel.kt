package com.example.habittracker.ui.feed

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CreatePostViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepository.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _postCreatedEvent = MutableSharedFlow<Boolean>()
    val postCreatedEvent = _postCreatedEvent.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun createPost(content: String, imageUri: Uri?) {
        if (_isLoading.value == true) return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val context = getApplication<Application>()

                // Get user info with fallback
                var userName = UserPreferences.getUserName(context)
                var userAvatar = UserPreferences.getUserAvatar(context)
                val currentUserId = auth.currentUser?.uid ?: run {
                    _isLoading.value = false
                    _errorMessage.emit("User not logged in")
                    return@launch
                }

                // Fallback to FirebaseAuth if prefs are missing
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    if (userName == "You" || userName.isEmpty()) {
                        userName = currentUser.displayName ?: "User"
                    }
                    if (userAvatar.isEmpty()) {
                        userAvatar = currentUser.photoUrl?.toString() ?: ""
                    }
                }

                val newPost = Post(
                    userId = currentUserId,
                    authorName = userName,
                    authorAvatarUrl = userAvatar.ifEmpty { null },
                    content = content,
                    timestamp = System.currentTimeMillis()
                )

                val result = postRepository.createPost(context, newPost, imageUri)

                _isLoading.value = false

                result.onSuccess {
                    _postCreatedEvent.emit(true)
                }.onFailure { e ->
                    _errorMessage.emit(e.message ?: "Failed to create post")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.emit(e.message ?: "Unknown error")
                e.printStackTrace()
            }
        }
    }
}
