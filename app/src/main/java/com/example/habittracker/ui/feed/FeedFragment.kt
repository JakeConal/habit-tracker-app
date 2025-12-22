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
import com.example.habittracker.utils.UserPreferences
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

        // Initialize user name if not set
        initializeUserIfNeeded()

        setupViews(view)
        setupRecyclerView(view)
        loadSampleData()
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
        // Get current user name for consistency
        val currentUserName = UserPreferences.getUserName(requireContext())

        val samplePosts = listOf(
            Post(
                id = "1",
                authorName = "John Doe",
                authorAvatar = "",
                timestamp = "2 hours ago",
                content = "Just completed my 30-day meditation challenge! Feeling amazing! 🧘‍♂️",
                imageUrl = null,
                likesCount = 24,
                commentsCount = 3,
                isLiked = true,
                comments = listOf(
                    Comment(
                        id = "c1_1",
                        authorName = "Alice Johnson",
                        authorAvatar = "",
                        timestamp = "1h ago",
                        content = "Great job! Keep it up! 👏",
                        likesCount = 5,
                        isLiked = false,
                        replies = listOf(
                            CommentReply(
                                id = "r1",
                                authorName = "John Doe", // Post author replies
                                authorAvatar = "",
                                timestamp = "30m ago",
                                content = "Thank you so much! 🙏",
                                likesCount = 2,
                                isLiked = false,
                                replies = emptyList()
                            )
                        )
                    ),
                    Comment(
                        id = "c1_2",
                        authorName = "Bob Smith",
                        authorAvatar = "",
                        timestamp = "3h ago",
                        content = "Inspiring! I should try this challenge too.",
                        likesCount = 3,
                        isLiked = true,
                        replies = listOf(
                            CommentReply(
                                id = "r2",
                                authorName = "John Doe", // Post author encourages
                                authorAvatar = "",
                                timestamp = "2h ago",
                                content = "You definitely should! It changed my life. Let me know if you need any tips! 💪",
                                likesCount = 1,
                                isLiked = false,
                                replies = listOf(
                                    CommentReply(
                                        id = "r2_1",
                                        authorName = "Bob Smith",
                                        authorAvatar = "",
                                        timestamp = "1h ago",
                                        content = "Thanks! I'll start tomorrow!",
                                        likesCount = 0,
                                        isLiked = false,
                                        replies = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Comment(
                        id = "c1_3",
                        authorName = "Carol White",
                        authorAvatar = "",
                        timestamp = "5h ago",
                        content = "Amazing progress! How did you stay motivated?",
                        likesCount = 2,
                        isLiked = false,
                        replies = listOf(
                            CommentReply(
                                id = "r3",
                                authorName = "John Doe", // Post author answers question
                                authorAvatar = "",
                                timestamp = "4h ago",
                                content = "I set small daily goals and tracked them! Also having an accountability partner helped a lot.",
                                likesCount = 4,
                                isLiked = false,
                                replies = listOf(
                                    CommentReply(
                                        id = "r3_1",
                                        authorName = "Carol White",
                                        authorAvatar = "",
                                        timestamp = "3h ago",
                                        content = "That's a great tip! Thanks for sharing!",
                                        likesCount = 1,
                                        isLiked = false,
                                        replies = emptyList()
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            Post(
                id = "2",
                authorName = "Jane Smith",
                authorAvatar = "",
                timestamp = "5 hours ago",
                content = "Morning run done! Starting the day with positive energy ☀️🏃‍♀️",
                imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8",
                likesCount = 18,
                commentsCount = 2,
                isLiked = false,
                comments = listOf(
                    Comment(
                        id = "c2_1",
                        authorName = currentUserName,
                        authorAvatar = "",
                        timestamp = "4h ago",
                        content = "Great start to the day! 🌅",
                        likesCount = 1,
                        isLiked = true,
                        replies = listOf(
                            CommentReply(
                                id = "r4",
                                authorName = "Jane Smith", // Post author replies to current user
                                authorAvatar = "",
                                timestamp = "3h ago",
                                content = "Thanks! Hope you have a great day too! 😊",
                                likesCount = 0,
                                isLiked = false,
                                replies = emptyList()
                            )
                        )
                    ),
                    Comment(
                        id = "c2_2",
                        authorName = "Mike Wilson",
                        authorAvatar = "",
                        timestamp = "3h ago",
                        content = "Keep up the good work!",
                        likesCount = 2,
                        isLiked = false,
                        replies = emptyList()
                    )
                )
            ),
            Post(
                id = "3",
                authorName = "Mike Johnson",
                authorAvatar = "",
                timestamp = "1 day ago",
                content = "Week 3 of my fitness journey. Progress is slow but steady. Keep pushing! 💪",
                imageUrl = null,
                likesCount = 42,
                commentsCount = 1,
                isLiked = true,
                comments = listOf(
                    Comment(
                        id = "c3_1",
                        authorName = currentUserName,
                        authorAvatar = "",
                        timestamp = "20h ago",
                        content = "You got this! Consistency is key! 🔥",
                        likesCount = 3,
                        isLiked = false,
                        replies = listOf(
                            CommentReply(
                                id = "r5",
                                authorName = "Mike Johnson", // Post author thanks current user
                                authorAvatar = "",
                                timestamp = "18h ago",
                                content = "Thank you for the encouragement! Really appreciate it! 💯",
                                likesCount = 1,
                                isLiked = false,
                                replies = emptyList()
                            )
                        )
                    )
                )
            )
        )

        postAdapter.submitList(samplePosts)
    }
}
