package com.example.habittracker.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        _isLoading.value = true
        // Fetch posts from repository
        viewModelScope.launch {
            val result = repository.getAllPosts()

            _isLoading.value = false

            result.onSuccess { postList ->
                _posts.value = postList
            }.onFailure { e ->
                // Log error
                e.printStackTrace()
            }
        }
    }
}
