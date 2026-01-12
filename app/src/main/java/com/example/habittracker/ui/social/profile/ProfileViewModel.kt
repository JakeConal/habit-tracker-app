package com.example.habittracker.ui.social.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.FriendRequest
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FriendRepository
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class for RecyclerView items with multiple view types
 */
sealed class FriendListItem {
    object SearchHeader : FriendListItem()
    
    data class SectionHeader(
        val title: String,
        val count: Int,
        val showBadge: Boolean = false
    ) : FriendListItem()
    
    data class RequestItem(val request: FriendRequest) : FriendListItem()
    
    data class FriendItem(val friend: User) : FriendListItem()

    data class GlobalUserItem(val user: User) : FriendListItem()
    
    data class EmptyState(val message: String) : FriendListItem()
}

/**
 * ProfileViewModel - Manages profile screen data and state
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository = PostRepository.getInstance()
    private val friendRepository = FriendRepository.getInstance()
    private val userRepository = FirestoreUserRepository.getInstance()

    // Current user data
    private val _userName = MutableStateFlow(UserPreferences.getUserName(application))
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("user@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userAvatarUrl = MutableStateFlow(UserPreferences.getUserAvatar(application))
    val userAvatarUrl: StateFlow<String> = _userAvatarUrl.asStateFlow()

    // Tab selection state
    enum class ProfileTab {
        MY_POST,
        MY_FRIENDS
    }

    private val _selectedTab = MutableStateFlow(ProfileTab.MY_POST)
    val selectedTab: StateFlow<ProfileTab> = _selectedTab.asStateFlow()

    // Cached data
    private var cachedUserPosts: List<Post> = emptyList()
    private var cachedFriends: List<User> = emptyList()
    private var cachedRequests: List<FriendRequest> = emptyList()

    // Filtered posts based on selected tab
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered friend list items for RecyclerView
    private val _filteredFriendListItems = MutableStateFlow<List<FriendListItem>>(emptyList())
    val filteredFriendListItems: StateFlow<List<FriendListItem>> = _filteredFriendListItems.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Toast message for one-off events
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val currentUserId: String
        get() {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            return auth.currentUser?.uid ?: UserPreferences.getUserId(getApplication())
        }

    init {
        // Fetch real data on init
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: UserPreferences.getUserId(application)

        // Update user info from auth if available
         auth.currentUser?.let { user ->
            _userName.value = user.displayName ?: _userName.value
            _userEmail.value = user.email ?: _userEmail.value
            _userAvatarUrl.value = user.photoUrl?.toString() ?: _userAvatarUrl.value
        }

        fetchUserPosts(userId)
        fetchFriendsAndRequests()
    }

    fun selectTab(tab: ProfileTab) {
        _selectedTab.value = tab
        updatePostsForTab()
        if (tab == ProfileTab.MY_FRIENDS) {
            updateFriendListUI()
        }
    }

    private fun updatePostsForTab() {
        _posts.value = when (_selectedTab.value) {
            ProfileTab.MY_POST -> cachedUserPosts
            ProfileTab.MY_FRIENDS -> emptyList()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Debounce could be added here, for now direct call
        searchAndFilter(query)
    }

    private fun searchAndFilter(query: String) {
        viewModelScope.launch {
            val q = query.lowercase().trim()
            val items = mutableListOf<FriendListItem>()
            items.add(FriendListItem.SearchHeader)

            // 1. Filter local friends and requests
            val filteredRequests = cachedRequests.filter { it.senderName.lowercase().contains(q) }
            val filteredFriends = cachedFriends.filter { it.name.lowercase().contains(q) }

            // Add Requests Section
            if (filteredRequests.isNotEmpty()) {
                items.add(FriendListItem.SectionHeader("Requests", filteredRequests.size, true))
                filteredRequests.forEach { items.add(FriendListItem.RequestItem(it)) }
            }

            // Add Friends Section
            if (filteredFriends.isNotEmpty()) {
                items.add(FriendListItem.SectionHeader("My Friends (${filteredFriends.size})", filteredFriends.size))
                filteredFriends.forEach { items.add(FriendListItem.FriendItem(it)) }
            }

            // 2. Global Search (only if query is not empty)
            if (q.isNotEmpty()) {
                _isLoading.value = true
                val globalResults = userRepository.searchUsers(query)
                _isLoading.value = false
                
                // Exclude self, existing friends, and people who sent requests
                val friendIds = cachedFriends.map { it.id }.toSet()
                val requesterIds = cachedRequests.map { it.senderId }.toSet()
                
                val nonFriendResults: List<User> = globalResults.filter { user ->
                    user.id != currentUserId &&
                    !friendIds.contains(user.id) &&
                    !requesterIds.contains(user.id)
                }

                if (nonFriendResults.isNotEmpty()) {
                    items.add(FriendListItem.SectionHeader("Global Search Results", nonFriendResults.size))
                    nonFriendResults.forEach { user -> items.add(FriendListItem.GlobalUserItem(user)) }
                } else if (filteredRequests.isEmpty() && filteredFriends.isEmpty()) {
                     items.add(FriendListItem.EmptyState("No results found for \"$query\""))
                }
            } else {
                if (filteredRequests.isEmpty() && filteredFriends.isEmpty()) {
                    items.add(FriendListItem.EmptyState("No friends yet. Search to add new friends!"))
                }
            }

            _filteredFriendListItems.value = items
        }
    }
    
    // For simple UI refresh without network call (when just clearing search)
    private fun updateFriendListUI() {
        searchAndFilter(_searchQuery.value)
    }

    fun fetchFriendsAndRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cachedRequests = friendRepository.getFriendRequests()
                cachedFriends = friendRepository.getFriends()
                updateFriendListUI()
            } catch (e: Exception) {
                _error.value = "Failed to load friends"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendFriendRequest(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = friendRepository.sendFriendRequest(user.id)
            _isLoading.value = false
            if (success) {
                _message.value = "Friend request sent to ${user.name}"
                // Optimistic UI update or refresh? Refresh is safer to ensure state consistency
                fetchFriendsAndRequests() // Refresh to exclude them from global search if needed, though they go to "sent" state which we don't display yet.
                // Or just show toast.
            } else {
                _error.value = "Failed to send request"
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
             val success = friendRepository.acceptFriendRequest(request.id)
             if (success) {
                 _message.value = "Friend request accepted"
                 fetchFriendsAndRequests()
             } else {
                 _error.value = "Failed to accept request"
             }
        }
    }

    fun rejectFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            val success = friendRepository.rejectFriendRequest(request.id)
            if (success) {
                fetchFriendsAndRequests()
            } else {
                 _message.value = "Failed to reject request"
            }
        }
    }

    fun unfriend(user: User) {
        viewModelScope.launch {
            val success = friendRepository.unfriend(user.id)
            if (success) {
                _message.value = "Unfriended ${user.name}"
                fetchFriendsAndRequests()
            } else {
                _error.value = "Failed to unfriend"
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }

    fun toggleLike(postId: String) {
        val currentPosts = _posts.value.toMutableList()
        val postIndex = currentPosts.indexOfFirst { it.id == postId }
        
        if (postIndex != -1) {
            val post = currentPosts[postIndex]
            val isLiked = post.likedBy.contains(currentUserId)

            // Local update logic
            val newLikedBy = if (isLiked) post.likedBy - currentUserId else post.likedBy + currentUserId
            val newLikeCount = if (isLiked) kotlin.math.max(0, post.likeCount - 1) else post.likeCount + 1

            val updatedPost = post.copy(
                likeCount = newLikeCount,
                likedBy = newLikedBy
            )
            currentPosts[postIndex] = updatedPost
            _posts.value = currentPosts
            cachedUserPosts = currentPosts

            // Server update
            viewModelScope.launch {
                postRepository.toggleLikePost(postId, !isLiked)
            }
        }
    }

    fun updatePost(postId: String, commentCount: Int, likeCount: Int, isLiked: Boolean) {
        val currentPosts = _posts.value.toMutableList()
        val postIndex = currentPosts.indexOfFirst { it.id == postId }

        if (postIndex != -1) {
            val post = currentPosts[postIndex]

            // Calculate new likedBy based on isLiked status
            val newLikedBy = if (isLiked) {
                 if (!post.likedBy.contains(currentUserId)) post.likedBy + currentUserId else post.likedBy
            } else {
                 post.likedBy - currentUserId
            }

            val updatedPost = post.copy(
                commentCount = commentCount,
                likeCount = likeCount,
                likedBy = newLikedBy
            )
            currentPosts[postIndex] = updatedPost
            _posts.value = currentPosts
            cachedUserPosts = currentPosts
        }
    }

    fun refreshPosts() {
        fetchUserPosts(currentUserId)
    }

    fun fetchUserPosts(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = postRepository.getPostsByUser(userId)

            result.onSuccess { userPosts ->
                cachedUserPosts = userPosts
                updatePostsForTab()
            }.onFailure { e ->
                _error.value = e.message
            }

            _isLoading.value = false
        }
    }
}
