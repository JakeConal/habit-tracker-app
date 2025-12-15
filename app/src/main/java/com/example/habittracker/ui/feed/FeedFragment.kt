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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.google.android.material.card.MaterialCardView

class FeedFragment : Fragment() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var cardCreatePost: MaterialCardView

    private val createPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val newPost = data.getParcelableExtra<Post>(CreatePostActivity.EXTRA_NEW_POST)
                newPost?.let { post ->
                    addNewPostToFeed(post)
                }
            }
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
                val updatedComments = data.getParcelableArrayListExtra<Comment>(CommentsActivity.RESULT_COMMENTS) ?: arrayListOf()

                // Update the post in the list
                updatePostInList(postId, newCommentCount, newLikeCount, isLiked, updatedComments)
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

        setupViews(view)
        setupRecyclerView(view)
        loadSampleData()
    }

    private fun setupViews(view: View) {
        cardCreatePost = view.findViewById(R.id.cardCreatePost)

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

    private fun openCreatePostActivity() {
        val intent = Intent(requireContext(), CreatePostActivity::class.java)
        createPostLauncher.launch(intent)
    }

    private fun addNewPostToFeed(newPost: Post) {
        val currentList = postAdapter.currentList.toMutableList()
        // Add new post at the beginning
        currentList.add(0, newPost)
        postAdapter.submitList(currentList)

        // Scroll to top to show the new post
        rvFeed.scrollToPosition(0)
    }

    private fun setupRecyclerView(view: View) {
        rvFeed = view.findViewById(R.id.rvFeed)
        postAdapter = PostAdapter(
            onLikeClick = { post ->
                // Toggle like status
                toggleLike(post)
            },
            onCommentClick = { post ->
                // Open comments activity
                openCommentsActivity(post)
            },
            onMoreOptionsClick = { post ->
                // Handle more options click
                Toast.makeText(context, "More options for ${post.authorName}'s post", Toast.LENGTH_SHORT).show()
            }
        )

        rvFeed.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun toggleLike(post: Post) {
        // Toggle like status
        val updatedPost = post.copy(
            isLiked = !post.isLiked,
            likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
        )

        // Update the list
        val currentList = postAdapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == post.id }
        if (index != -1) {
            currentList[index] = updatedPost
            postAdapter.submitList(currentList)
        }

        // TODO: Send like status to server
    }

    private fun openCommentsActivity(post: Post) {
        val intent = Intent(requireContext(), CommentsActivity::class.java).apply {
            putExtra(CommentsActivity.EXTRA_POST_ID, post.id)
            putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
            putExtra(CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatar)
            putExtra(CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
            putExtra(CommentsActivity.EXTRA_CONTENT, post.content)
            putExtra(CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
            putExtra(CommentsActivity.EXTRA_LIKES_COUNT, post.likesCount)
            putExtra(CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentsCount)
            putExtra(CommentsActivity.EXTRA_IS_LIKED, post.isLiked)
            putParcelableArrayListExtra(CommentsActivity.EXTRA_COMMENTS, ArrayList(post.comments))
        }
        commentsLauncher.launch(intent)
    }

    private fun updatePostInList(postId: String?, newCommentCount: Int, newLikeCount: Int, isLiked: Boolean, updatedComments: List<Comment>) {
        if (postId == null) return

        val currentList = postAdapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == postId }

        if (index != -1) {
            val updatedPost = currentList[index].copy(
                commentsCount = newCommentCount,
                likesCount = newLikeCount,
                isLiked = isLiked,
                comments = updatedComments
            )
            currentList[index] = updatedPost
            postAdapter.submitList(currentList)
        }
    }

    private fun loadSampleData() {
        val samplePosts = listOf(
            Post(
                id = "1",
                authorName = "John Doe",
                authorAvatar = "",
                timestamp = "2 hours ago",
                content = "Just completed my 30-day meditation challenge! Feeling amazing! 🧘‍♂️",
                imageUrl = null,
                likesCount = 24,
                commentsCount = 5,
                isLiked = true
            ),
            Post(
                id = "2",
                authorName = "Jane Smith",
                authorAvatar = "",
                timestamp = "5 hours ago",
                content = "Morning run done! Starting the day with positive energy ☀️🏃‍♀️",
                imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8",
                likesCount = 18,
                commentsCount = 3,
                isLiked = false
            ),
            Post(
                id = "3",
                authorName = "Mike Johnson",
                authorAvatar = "",
                timestamp = "1 day ago",
                content = "Week 3 of my fitness journey. Progress is slow but steady. Keep pushing! 💪",
                imageUrl = null,
                likesCount = 42,
                commentsCount = 8,
                isLiked = true
            )
        )

        postAdapter.submitList(samplePosts)
    }
}
