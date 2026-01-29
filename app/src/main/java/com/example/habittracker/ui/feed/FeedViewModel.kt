package com.example.habittracker.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class FeedViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()
    private val PAGE_SIZE = 10L

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: Boolean get() = _hasMoreData.value

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val posts: StateFlow<List<Post>> = combine(_currentLimit, _refreshTrigger) { limit, _ -> limit }
        .flatMapLatest { limit ->
            repository.listenToPosts(limit)
                .onStart {
                    // Only show loading if we don't have enough data yet
                    if (_currentLimit.value <= PAGE_SIZE) {
                        _isLoading.value = true
                    }
                }
                .onEach { postsList ->
                    _isLoading.value = false
                    // If we receive fewer posts than the current limit, we've reached the end
                    if (postsList.size < limit) {
                        _hasMoreData.value = false
                    } else {
                        _hasMoreData.value = true
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val votedChallengeIds: StateFlow<List<String>> =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            com.example.habittracker.data.repository.FirestoreUserRepository.getInstance()
                .listenToUser(userId)
                .map { it?.votedChallengeIds ?: emptyList() }
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        } ?: MutableStateFlow<List<String>>(emptyList()).asStateFlow()

    fun fetchPosts(isRefresh: Boolean = false) {
        if (_isLoading.value && !isRefresh) return

        if (isRefresh) {
            _hasMoreData.value = true
            if (_currentLimit.value == PAGE_SIZE) {
                // Trigger a re-load even if limit is same by updating the refresh trigger
                _refreshTrigger.value = System.currentTimeMillis()
            } else {
                _currentLimit.value = PAGE_SIZE
            }
            return
        }

        if (!_hasMoreData.value) return

        // Set loading to true to prevent multiple triggers from FeedFragment
        _isLoading.value = true
        _currentLimit.value += PAGE_SIZE
    }
}
