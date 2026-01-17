package com.example.habittracker.ui.feed

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.utils.UserPreferences
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import android.widget.ImageView

class FeedFragment : Fragment() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var cardCreatePost: MaterialCardView
    private lateinit var swipeRefreshFeed: SwipeRefreshLayout

    private val createPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh feed when returning from CreatePostActivity
            refreshPosts()
        }
    }

    private val commentsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val postId = data.getStringExtra(CommentsActivity.RESULT_POST_ID)
                val newCommentCount = data.getIntExtra(CommentsActivity.RESULT_COMMENT_COUNT, 0)
                val newLikeCount = data.getIntExtra(CommentsActivity.RESULT_LIKE_COUNT, 0)
                val isLiked = data.getBooleanExtra(CommentsActivity.RESULT_IS_LIKED, false)

                // Update the post in the list
                updatePostInList(postId, newCommentCount, newLikeCount, isLiked)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize user name if not set
        initializeUserIfNeeded()

        setupViews(view)
        setupRecyclerView(view)
        refreshPosts()
    }

    override fun onResume() {
        super.onResume()
        updateUserAvatar()
    }

    private fun initializeUserIfNeeded() {
        val currentName = UserPreferences.getUserName(requireContext())
        // If user name is still default "You", we can prompt them to set it
        // For now, we'll set a default name for testing
        if (currentName == "You") {
            // You can show a dialog here to ask for user name
            // For demo purposes, setting a default name
            UserPreferences.saveUserName(requireContext(), "Current User")
        }
    }

    private fun setupViews(view: View) {
        cardCreatePost = view.findViewById(R.id.cardCreatePost)
        swipeRefreshFeed = view.findViewById(R.id.swipeRefreshFeed)

        updateUserAvatar()

        swipeRefreshFeed.setOnRefreshListener {
            refreshPosts()
        }

        cardCreatePost.setOnClickListener {
            openCreatePostActivity()
        }

        view.findViewById<View>(R.id.ivCameraIcon)?.setOnClickListener {
            openCreatePostActivity()
        }

        view.findViewById<View>(R.id.tvShareProgress)?.setOnClickListener {
            openCreatePostActivity()
        }
    }

    private fun updateUserAvatar() {
        view?.let { view ->
            val ivCurrentUserAvatar = view.findViewById<ImageView>(R.id.ivCurrentUserAvatar)
            val avatarUrl = UserPreferences.getUserAvatar(requireContext())

            if (ivCurrentUserAvatar != null && avatarUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivCurrentUserAvatar)
            }
        }
    }

    private fun openCreatePostActivity() {
        val intent = Intent(requireContext(), CreatePostActivity::class.java)
        createPostLauncher.launch(intent)
    }

    private val currentUserId: String
        get() {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            return auth.currentUser?.uid ?: UserPreferences.getUserId(requireContext())
        }

    private fun setupRecyclerView(view: View) {
        rvFeed = view.findViewById(R.id.rvFeed)
        postAdapter = PostAdapter(
            currentUserId = currentUserId,
            onLikeClick = { post ->
                // Toggle like status
                toggleLike(post)
            },
            onCommentClick = { post ->
                // Open comments activity
                openCommentsActivity(post)
            },
            onAuthorClick = { userId ->
                if (userId == currentUserId) {
                    findNavController().navigate(R.id.nav_profile)
                } else {
                    val bundle = Bundle().apply {
                        putString("friendId", userId)
                    }
                    findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
                }
            },
            onShareClick = { post ->
                // Open share to feed
                openCreatePostForSharing(post)
            },
            onMoreOptionsClick = { post, anchorView ->
                showPostOptions(post, anchorView)
            }
        )

        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun showPostOptions(post: Post, anchorView: View) {
        val popupMenu = android.widget.PopupMenu(requireContext(), anchorView)

        // Inflate logic based on ownership
        if (post.userId == currentUserId) {
            popupMenu.menu.add("Delete")
        } else {
            popupMenu.menu.add("Hide")
        }
        // popupMenu.menu.add("Share to Feed")
        // popupMenu.menu.add("Share Externally")

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Delete" -> {
                    deletePost(post)
                    true
                }
                "Hide" -> {
                    hidePost(post)
                    true
                }
                // "Share to Feed" -> {
                //     openCreatePostForSharing(post)
                //     true
                // }
                // "Share Externally" -> {
                //     // Update share count in backend
                //     lifecycleScope.launch {
                //         PostRepository.getInstance().sharePost(post.id)
                //     }

                //     val shareIntent = Intent(Intent.ACTION_SEND).apply {
                //         type = "text/plain"
                //         putExtra(Intent.EXTRA_SUBJECT, "Check out this habit update!")
                //         putExtra(Intent.EXTRA_TEXT, "${post.content}\n\nShared from Habit Tracker App")
                //     }
                //     startActivity(Intent.createChooser(shareIntent, "Share post via"))
                //     true
                // }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun openCreatePostForSharing(post: Post) {
        val intent = Intent(requireContext(), CreatePostActivity::class.java).apply {
            putExtra("EXTRA_SHARED_POST", post)
        }
        createPostLauncher.launch(intent)
    }

    private fun hidePost(post: Post) {
        lifecycleScope.launch {
            val result = PostRepository.getInstance().hidePost(post.id, currentUserId)
            if (result.isSuccess) {
                val currentList = postAdapter.currentList.toMutableList()
                val index = currentList.indexOfFirst { it.id == post.id }
                if (index != -1) {
                    currentList.removeAt(index)
                    postAdapter.submitList(currentList)
                    Toast.makeText(context, "Post hidden", Toast.LENGTH_SHORT).show()
                }
            } else {
                 Toast.makeText(context, "Failed to hide post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost(post: Post) {
        lifecycleScope.launch {
             val result = PostRepository.getInstance().deletePost(post.id)
             if (result.isSuccess) {
                 val currentList = postAdapter.currentList.toMutableList()
                 val index = currentList.indexOfFirst { it.id == post.id }
                 if (index != -1) {
                     currentList.removeAt(index)
                     postAdapter.submitList(currentList)
                     Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                 }
             } else {
                 Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
             }
        }
    }

    private fun toggleLike(post: Post) {
        val isLiked = post.likedBy.contains(currentUserId)

        // Toggle like status locally for UI responsiveness
        val newLikedBy = if (isLiked) post.likedBy - currentUserId else post.likedBy + currentUserId
        val newLikeCount = if (isLiked) maxOf(0, post.likeCount - 1) else post.likeCount + 1

        val updatedPost = post.copy(
            likedBy = newLikedBy,
            likeCount = newLikeCount
        )

        // Update the list
        val currentList = postAdapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == post.id }
        if (index != -1) {
            currentList[index] = updatedPost
            postAdapter.submitList(currentList)
        }

        // Get user info for notification
        val senderName = UserPreferences.getUserName(requireContext())
        val senderAvatar = UserPreferences.getUserAvatar(requireContext())

        // Send like status to server
        lifecycleScope.launch {
            val result = PostRepository.getInstance().toggleLikePost(post.id, !isLiked, senderName, senderAvatar)
            if (result.isFailure) {
                 // Revert if failed
                 val revertedList = postAdapter.currentList.toMutableList()
                 val revertedIndex = revertedList.indexOfFirst { it.id == post.id }
                 if (revertedIndex != -1) {
                     revertedList[revertedIndex] = post
                     postAdapter.submitList(revertedList)
                     Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show()
                 }
            }
        }
    }

    private fun openCommentsActivity(post: Post) {
        val isLiked = post.likedBy.contains(currentUserId)

        val intent = Intent(requireContext(), CommentsActivity::class.java).apply {
            putExtra(CommentsActivity.EXTRA_POST_ID, post.id)
            putExtra(CommentsActivity.EXTRA_POST_USER_ID, post.userId)
            putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
            putExtra(CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatarUrl)
            putExtra(CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
            putExtra(CommentsActivity.EXTRA_CONTENT, post.content)
            putExtra(CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
            putExtra(CommentsActivity.EXTRA_LIKES_COUNT, post.likeCount)
            putExtra(CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentCount)
            putExtra(CommentsActivity.EXTRA_IS_LIKED, isLiked)

            // Pass shared post data
            if (!post.originalPostId.isNullOrEmpty()) {
                putExtra(CommentsActivity.EXTRA_ORIGINAL_POST_ID, post.originalPostId)
                putExtra(CommentsActivity.EXTRA_ORIGINAL_USER_ID, post.originalUserId)
                putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_NAME, post.originalAuthorName)
                putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_AVATAR, post.originalAuthorAvatarUrl)
                putExtra(CommentsActivity.EXTRA_ORIGINAL_CONTENT, post.originalContent)
                putExtra(CommentsActivity.EXTRA_ORIGINAL_IMAGE_URL, post.originalImageUrl)
            }
        }
        commentsLauncher.launch(intent)
    }

    private fun updatePostInList(postId: String?, newCommentCount: Int, newLikeCount: Int, isLiked: Boolean) {
        if (postId == null) return

        val currentList = postAdapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == postId }

        if (index != -1) {
            val currentPost = currentList[index]

            // Update likedBy list based on isLiked boolean
             val newLikedBy = if (isLiked) {
                if (!currentPost.likedBy.contains(currentUserId)) currentPost.likedBy + currentUserId else currentPost.likedBy
            } else {
                currentPost.likedBy - currentUserId
            }

            val updatedPost = currentList[index].copy(
                commentCount = newCommentCount,
                likeCount = newLikeCount,
                likedBy = newLikedBy
                // comments field doesn't exist in Post
            )
            currentList[index] = updatedPost
            postAdapter.submitList(currentList)
        }
    }

    private fun refreshPosts() {
        swipeRefreshFeed.isRefreshing = true
        lifecycleScope.launch {
            val result = PostRepository.getInstance().getAllPosts()

            result.onSuccess { dataPosts ->
                val filteredPosts = dataPosts.filter { !it.hiddenBy.contains(currentUserId) }
                postAdapter.submitList(filteredPosts)
                swipeRefreshFeed.isRefreshing = false
            }.onFailure { e ->
                // Handle error
                e.printStackTrace()
                swipeRefreshFeed.isRefreshing = false
                Toast.makeText(context, "Failed to refresh feed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
