package com.example.habittracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Friend data model
 */
data class Friend(
    val id: String,
    val userId: String,
    val name: String,
    val avatarUrl: String,
    val currentStreak: Int
)

/**
 * Friend Request data model
 */
data class FriendRequest(
    val id: String,
    val userId: String,
    val name: String,
    val avatarUrl: String,
    val mutualFriendsCount: Int
)

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
    
    data class FriendItem(val friend: Friend) : FriendListItem()
    
    data class EmptyState(val message: String) : FriendListItem()
}

/**
 * ProfileViewModel - Manages profile screen data and state
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepository.getInstance()

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

    // Store fetched posts here instead of mock data
    private var cachedUserPosts: List<Post> = emptyList()

    // Filtered posts based on selected tab
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Friend requests
    private val allFriendRequests = listOf(
        FriendRequest(
            id = "req_001",
            userId = "user_101",
            name = "Emma Thompson",
            avatarUrl = "",
            mutualFriendsCount = 12
        ),
        FriendRequest(
            id = "req_002",
            userId = "user_102",
            name = "Michael Chen",
            avatarUrl = "",
            mutualFriendsCount = 8
        )
    )

    // Friends list
    private val allFriends = listOf(
        Friend(
            id = "friend_001",
            userId = "user_201",
            name = "Emma Thompson",
            avatarUrl = "",
            currentStreak = 45
        ),
        Friend(
            id = "friend_002",
            userId = "user_202",
            name = "Michael Chen",
            avatarUrl = "",
            currentStreak = 32
        ),
        Friend(
            id = "friend_003",
            userId = "user_203",
            name = "Olivia Martinez",
            avatarUrl = "",
            currentStreak = 28
        ),
        Friend(
            id = "friend_004",
            userId = "user_204",
            name = "David Park",
            avatarUrl = "",
            currentStreak = 21
        ),
        Friend(
            id = "friend_005",
            userId = "user_205",
            name = "Sophia Anderson",
            avatarUrl = "",
            currentStreak = 15
        )
    )

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
    }

    fun selectTab(tab: ProfileTab) {
        _selectedTab.value = tab
        updatePostsForTab()
        if (tab == ProfileTab.MY_FRIENDS) {
            filterFriendList()
        }
    }

    private fun updatePostsForTab() {
        _posts.value = when (_selectedTab.value) {
            ProfileTab.MY_POST -> cachedUserPosts // Use cached real data
            ProfileTab.MY_FRIENDS -> emptyList()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterFriendList()
    }

    private fun filterFriendList() {
        val query = _searchQuery.value.lowercase().trim()
        val items = mutableListOf<FriendListItem>()

        // Add search header
        items.add(FriendListItem.SearchHeader)

        // Filter requests
        val filteredRequests = if (query.isEmpty()) {
            allFriendRequests
        } else {
            allFriendRequests.filter { it.name.lowercase().contains(query) }
        }

        // Filter friends
        val filteredFriends = if (query.isEmpty()) {
            allFriends
        } else {
            allFriends.filter { it.name.lowercase().contains(query) }
        }

        // Add requests section
        if (filteredRequests.isNotEmpty()) {
            items.add(
                FriendListItem.SectionHeader(
                    title = "Requests",
                    count = filteredRequests.size,
                    showBadge = true
                )
            )
            filteredRequests.forEach { items.add(FriendListItem.RequestItem(it)) }
        }

        // Add friends section
        if (filteredFriends.isNotEmpty()) {
            items.add(
                FriendListItem.SectionHeader(
                    title = "My Friends (${filteredFriends.size})",
                    count = filteredFriends.size,
                    showBadge = false
                )
            )
            filteredFriends.forEach { items.add(FriendListItem.FriendItem(it)) }
        }

        // Add empty state if no results
        if (filteredRequests.isEmpty() && filteredFriends.isEmpty() && query.isNotEmpty()) {
            items.add(FriendListItem.EmptyState("No friends found"))
        }

        _filteredFriendListItems.value = items
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
                // Pass !isLiked because if it WAS liked, we want to dislike (false), and vice-versa
                repository.toggleLikePost(postId, !isLiked)
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

    fun fetchUserPosts(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Call repository for real data
            val result = repository.getPostsByUser(userId)

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
