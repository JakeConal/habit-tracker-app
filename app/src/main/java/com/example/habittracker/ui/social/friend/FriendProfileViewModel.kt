package com.example.habittracker.ui.social.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.FriendProfile
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.data.repository.FriendRepository
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.ui.social.profile.FriendListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * FriendProfileViewModel - Manages friend profile screen data and state
 */
class FriendProfileViewModel : ViewModel() {

    private val userRepository = FirestoreUserRepository.getInstance()
    private val postRepository = PostRepository.getInstance()
    private val friendRepository = FriendRepository.getInstance()

    // Tab selection state
    enum class ProfileTab {
        MY_POST,
        MY_FRIENDS
    }

    private val _selectedTab = MutableStateFlow(ProfileTab.MY_POST)
    val selectedTab: StateFlow<ProfileTab> = _selectedTab.asStateFlow()

    // Friend data
    private val _friendProfile = MutableStateFlow<FriendProfile?>(null)
    val friendProfile: StateFlow<FriendProfile?> = _friendProfile.asStateFlow()

    // Posts data
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Friends list data
    private val _friendListItems = MutableStateFlow<List<FriendListItem>>(emptyList())
    val friendListItems: StateFlow<List<FriendListItem>> = _friendListItems.asStateFlow()

    // Empty state
    private val _showEmptyState = MutableStateFlow(false)
    val showEmptyState: StateFlow<Boolean> = _showEmptyState.asStateFlow()

    private val _emptyStateMessage = MutableStateFlow("")
    val emptyStateMessage: StateFlow<String> = _emptyStateMessage.asStateFlow()

    private var currentFriendId: String = ""

    /**
     * Load friend profile data by ID
     */
    fun loadFriendProfile(friendId: String) {
        currentFriendId = friendId
        
        viewModelScope.launch {
            // 1. Fetch User Info
            val user = userRepository.getUserById(friendId)
            if (user != null) {
                _friendProfile.value = FriendProfile(
                    id = user.id,
                    userId = user.id,
                    name = user.name,
                    email = user.email ?: "",
                    avatarUrl = user.avatarUrl ?: ""
                )
            }

            // 2. Fetch Posts
            postRepository.getPostsByUser(friendId).onSuccess { userPosts ->
                _posts.value = userPosts
                updateEmptyState()
            }

            // 3. Fetch Friends
            val friends = friendRepository.getFriends(friendId)
            val items = friends.map { FriendListItem.FriendItem(it) }
            _friendListItems.value = if (items.isNotEmpty()) {
                items
            } else {
                emptyList() // Empty state handled in updateEmptyState or via specialized item
            }
            updateEmptyState()
        }
    }

    /**
     * Switch between tabs
     */
    fun selectTab(tab: ProfileTab) {
        _selectedTab.value = tab
        updateEmptyState()
    }

    /**
     * Update empty state based on selected tab
     */
    private fun updateEmptyState() {
        when (_selectedTab.value) {
            ProfileTab.MY_POST -> {
                _showEmptyState.value = _posts.value.isEmpty()
                _emptyStateMessage.value = "No posts yet"
            }
            ProfileTab.MY_FRIENDS -> {
                _showEmptyState.value = _friendListItems.value.isEmpty()
                _emptyStateMessage.value = "No friends yet"
            }
        }
    }
}
