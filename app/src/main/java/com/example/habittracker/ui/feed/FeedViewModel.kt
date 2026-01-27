package com.example.habittracker.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()
    private val PAGE_SIZE = 10L

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _votedChallengeIds = MutableStateFlow<List<String>>(emptyList())
    val votedChallengeIds: StateFlow<List<String>> = _votedChallengeIds.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private var _hasMoreData = true
    val hasMoreData: Boolean get() = _hasMoreData

    init {
        fetchPosts(isRefresh = true)
        fetchVotedChallenges()
    }

    fun fetchVotedChallenges() {
        viewModelScope.launch {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val user = com.example.habittracker.data.repository.FirestoreUserRepository.getInstance().getUserById(currentUserId)
            user?.let {
                _votedChallengeIds.value = it.votedChallengeIds
            }
        }
    }

    fun fetchPosts(isRefresh: Boolean = false) {
        if (_isLoading.value || (!isRefresh && !_hasMoreData)) return

        if (isRefresh) {
            lastDocument = null
            _hasMoreData = true
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.getPostsPaginated(PAGE_SIZE, lastDocument)

            _isLoading.value = false

            result.onSuccess { (newPosts, lastSnapshot) ->
                if (isRefresh) {
                    _posts.value = newPosts
                } else {
                    _posts.value = _posts.value + newPosts
                }

                lastDocument = lastSnapshot
                _hasMoreData = newPosts.size >= PAGE_SIZE
            }.onFailure { e ->
                // Log error
                e.printStackTrace()
            }
        }
    }
}
