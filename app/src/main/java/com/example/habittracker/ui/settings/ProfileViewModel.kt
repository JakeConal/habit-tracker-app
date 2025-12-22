package com.example.habittracker.ui.settings

import androidx.lifecycle.ViewModel
import com.example.habittracker.ui.feed.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ProfileViewModel - Manages profile screen data and state
 */
class ProfileViewModel : ViewModel() {

    // Current user data
    private val _userName = MutableStateFlow("Tanzir Fahad")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("fahaduxlab@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userAvatarUrl = MutableStateFlow("") // Empty for local placeholder
    val userAvatarUrl: StateFlow<String> = _userAvatarUrl.asStateFlow()

    private val currentUserId = "user_001" // Mock current user ID

    // Tab selection state
    enum class ProfileTab {
        MY_POST,
        MY_FRIENDS
    }

    private val _selectedTab = MutableStateFlow(ProfileTab.MY_POST)
    val selectedTab: StateFlow<ProfileTab> = _selectedTab.asStateFlow()

    // All posts
    private val allPosts = listOf(
        Post(
            id = "post_001",
            userId = "user_001",
            authorName = "Tanzir Fahad",
            authorAvatar = "",
            timestamp = "6 hours ago",
            content = "Healthy meal prep for the week done! Consistency is key to success 🥗💪",
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
            likesCount = 35,
            commentsCount = 8,
            isLiked = false
        ),
        Post(
            id = "post_002",
            userId = "user_001",
            authorName = "Tanzir Fahad",
            authorAvatar = "",
            timestamp = "1 day ago",
            content = "Morning meditation complete ✨ Starting the day with a clear mind helps me stay focused on my habits!",
            imageUrl = null,
            likesCount = 42,
            commentsCount = 12,
            isLiked = true
        ),
        Post(
            id = "post_003",
            userId = "user_001",
            authorName = "Tanzir Fahad",
            authorAvatar = "",
            timestamp = "2 days ago",
            content = "Just completed my 30-day challenge! 🎉 Feeling accomplished and ready for the next one!",
            imageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800",
            likesCount = 128,
            commentsCount = 24,
            isLiked = true
        )
    )

    // Filtered posts based on selected tab
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    init {
        updatePostsForTab()
    }

    fun selectTab(tab: ProfileTab) {
        _selectedTab.value = tab
        updatePostsForTab()
    }

    private fun updatePostsForTab() {
        _posts.value = when (_selectedTab.value) {
            ProfileTab.MY_POST -> allPosts.filter { it.userId == currentUserId }
            ProfileTab.MY_FRIENDS -> emptyList() // Not implemented yet
        }
    }

    fun toggleLike(postId: String) {
        val currentPosts = _posts.value.toMutableList()
        val postIndex = currentPosts.indexOfFirst { it.id == postId }
        
        if (postIndex != -1) {
            val post = currentPosts[postIndex]
            val updatedPost = post.copy(
                isLiked = !post.isLiked,
                likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
            )
            currentPosts[postIndex] = updatedPost
            _posts.value = currentPosts
        }
    }
}
