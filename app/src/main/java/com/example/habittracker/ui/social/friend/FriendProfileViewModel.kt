package com.example.habittracker.ui.social.friend

import androidx.lifecycle.ViewModel
import com.example.habittracker.data.model.FriendProfile
import com.example.habittracker.data.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FriendProfileViewModel - Manages friend profile screen data and state
 */
class FriendProfileViewModel : ViewModel() {

    // Tab selection state
    enum class ProfileTab {
        MY_POST,
        MY_FRIENDS
    }

    private val _selectedTab = MutableStateFlow(ProfileTab.MY_POST)
    val selectedTab: StateFlow<ProfileTab> = _selectedTab.asStateFlow()

    // Friend profile data
    private val _friendProfile = MutableStateFlow<FriendProfile?>(null)
    val friendProfile: StateFlow<FriendProfile?> = _friendProfile.asStateFlow()

    // Posts data
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Empty state
    private val _showEmptyState = MutableStateFlow(false)
    val showEmptyState: StateFlow<Boolean> = _showEmptyState.asStateFlow()

    private val _emptyStateMessage = MutableStateFlow("")
    val emptyStateMessage: StateFlow<String> = _emptyStateMessage.asStateFlow()

    /**
     * Load friend profile data by ID
     */
    fun loadFriendProfile(friendId: String) {
        // Mock data - In real implementation, this would fetch from repository
        _friendProfile.value = FriendProfile(
            id = "friend_001",
            userId = friendId,
            name = "Emma Thompson",
            email = "fahaduxlab@gmail.com",
            avatarUrl = "https://i.pravatar.cc/150?u=$friendId"
        )

        // Mock posts data
        val mockPosts = listOf(
            Post(
                id = "post_001",
                userId = friendId,
                authorName = "Emma Thompson",
                authorAvatarUrl = "https://i.pravatar.cc/150?u=$friendId",
                timestamp = System.currentTimeMillis() - 6 * 3600 * 1000,
                content = "Hit my reading goal for the month! 📚 Knowledge is power, keep learning every day.",
                imageUrl = null,
                likeCount = 28,
                commentCount = 5,
                likedBy = emptyList()
            ),
            Post(
                id = "post_002",
                userId = friendId,
                authorName = "Emma Thompson",
                authorAvatarUrl = "https://i.pravatar.cc/150?u=$friendId",
                timestamp = System.currentTimeMillis() - 24 * 3600 * 1000,
                content = "Morning run completed! 🏃‍♀️ Feeling energized and ready for the day ahead.",
                imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=800",
                likeCount = 45,
                commentCount = 8,
                likedBy = emptyList()
            ),
            Post(
                id = "post_003",
                userId = friendId,
                authorName = "Emma Thompson",
                authorAvatarUrl = "https://i.pravatar.cc/150?u=$friendId",
                timestamp = System.currentTimeMillis() - 3 * 24 * 3600 * 1000,
                content = "Trying out a new healthy recipe today! 🥗 Eating well is such an important habit.",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
                likeCount = 52,
                commentCount = 12,
                likedBy = listOf("current_user_id") // Simulate liked
            )
        )

        _posts.value = mockPosts
        updateEmptyState()
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
                _showEmptyState.value = true
                _emptyStateMessage.value = "Friend list view coming soon"
            }
        }
    }
}
