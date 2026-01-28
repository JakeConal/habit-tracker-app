package com.example.habittracker.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()
    private val PAGE_SIZE = 10L

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)

    val posts: StateFlow<List<Post>> = _currentLimit
        .flatMapLatest { limit ->
            repository.listenToPosts(limit)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _votedChallengeIds = MutableStateFlow<List<String>>(emptyList())
    val votedChallengeIds: StateFlow<List<String>> =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            com.example.habittracker.data.repository.FirestoreUserRepository.getInstance()
                .listenToUser(userId)
                .map { it?.votedChallengeIds ?: emptyList() }
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        } ?: MutableStateFlow<List<String>>(emptyList()).asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    init {
        // fetchVotedChallenges() - No longer needed as it's a real-time Flow
    }

    fun fetchPosts(isRefresh: Boolean = false) {
        if (_isLoading.value) return

        if (isRefresh) {
            _currentLimit.value = PAGE_SIZE
            _hasMoreData = true
            return
        }

        if (!_hasMoreData) return

        _isLoading.value = true

        // When using real-time listener with limit, we just increase the limit.
        // We need to check if we can actually load more.
        // A simple way is to check the current posts size.

        val currentPostsCount = posts.value.size
        if (currentPostsCount < _currentLimit.value) {
            // We already reached the end because Firestore returned fewer than requested
            _hasMoreData = false
            _isLoading.value = false
            return
        }

        _currentLimit.value += PAGE_SIZE
        _isLoading.value = false
    }
}
