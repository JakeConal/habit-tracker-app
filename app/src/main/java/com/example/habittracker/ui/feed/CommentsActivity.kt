package com.example.habittracker.ui.feed

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.habittracker.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.model.Comment
import com.example.habittracker.databinding.ActivityCommentsBinding
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.ui.main.MainActivity
import com.example.habittracker.ui.common.ImagePreviewActivity
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var commentAdapter: CommentAdapter
    private var post: Post? = null
    private var isLiked = false
    private var likeCount = 0
    private var commentCount = 0
    private var replyingToComment: Comment? = null
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            MainActivity.hideSystemUI(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Allow content to extend behind system bars and handle insets manually
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjust padding when keyboard appears
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, imeInsets.bottom)
            windowInsets
        }

        getPostFromIntent()
        setupViews()
        setupRecyclerView()
        loadComments()

        // Handle back press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnResult()
            }
        })
    }

    private fun getPostFromIntent() {
        // Get post data from intent
        val postId = intent.getStringExtra(EXTRA_POST_ID) ?: ""
        val authorName = intent.getStringExtra(EXTRA_AUTHOR_NAME) ?: ""
        val authorAvatar = intent.getStringExtra(EXTRA_AUTHOR_AVATAR) ?: ""
        // Timestamp is passed as Long from updated FeedFragment, but handling String fallback just in case
        val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val likesCount = intent.getIntExtra(EXTRA_LIKES_COUNT, 0)
        val commentsCount = intent.getIntExtra(EXTRA_COMMENTS_COUNT, 0)
        val isLikedExtra = intent.getBooleanExtra(EXTRA_IS_LIKED, false)

        val originalPostId = intent.getStringExtra(EXTRA_ORIGINAL_POST_ID)
        val originalUserId = intent.getStringExtra(EXTRA_ORIGINAL_USER_ID)
        val originalAuthorName = intent.getStringExtra(EXTRA_ORIGINAL_AUTHOR_NAME)
        val originalAuthorAvatar = intent.getStringExtra(EXTRA_ORIGINAL_AUTHOR_AVATAR)
        val originalContent = intent.getStringExtra(EXTRA_ORIGINAL_CONTENT)
        val originalImageUrl = intent.getStringExtra(EXTRA_ORIGINAL_IMAGE_URL)

        val existingComments = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(EXTRA_COMMENTS, Comment::class.java) ?: arrayListOf()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(EXTRA_COMMENTS) ?: arrayListOf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            arrayListOf<Comment>()
        }

        val currentUserId = UserPreferences.getUserId(this)

        post = Post(
            id = postId,
            userId = intent.getStringExtra(EXTRA_POST_USER_ID) ?: "", // Pass post owner ID
            authorName = authorName,
            authorAvatarUrl = authorAvatar,
            timestamp = timestamp,
            content = content,
            imageUrl = imageUrl,
            likeCount = likesCount,
            commentCount = commentsCount,
            // isLiked logic needs to be consistent. Constructing Post here is mainly for reference.
            // data.model.Post doesn't have isLiked, it has likedBy.
            // We can fake it or just use local isLiked variable.
            likedBy = if(isLikedExtra) listOf(currentUserId) else emptyList(),
            originalPostId = originalPostId,
            originalUserId = originalUserId,
            originalAuthorName = originalAuthorName,
            originalAuthorAvatarUrl = originalAuthorAvatar,
            originalContent = originalContent,
            originalImageUrl = originalImageUrl
        )

        isLiked = isLikedExtra
        likeCount = likesCount
        commentCount = commentsCount

        // Load existing comments
        comments.clear()
        comments.addAll(existingComments)
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            returnResult()
        }

        // Setup current user avatar in comment input
        val currentUserAvatar = UserPreferences.getUserAvatar(this)
        if (currentUserAvatar.isNotEmpty()) {
            Glide.with(this)
                .load(currentUserAvatar)
                .placeholder(R.drawable.ic_person)
                .into(binding.ivCurrentUserAvatar)
        } else {
            binding.ivCurrentUserAvatar.setImageResource(R.drawable.ic_person)
        }

        // Display post data
        post?.let { post ->
            binding.tvAuthorName.text = post.authorName

            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(post.timestamp))

            binding.tvContent.text = post.content
            binding.tvLikeCount.text = likeCount.toString()
            binding.tvCommentCount.text = commentCount.toString()
            binding.tvShareCount.text = post.shareCount.toString()

            // Avatar
            if (!post.authorAvatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(post.authorAvatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAuthorAvatar)
            } else {
                binding.ivAuthorAvatar.setImageResource(R.drawable.ic_person)
            }

            // Post image
            if (post.imageUrl != null) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(post.imageUrl)
                    .into(binding.ivPostImage)

                binding.ivPostImage.setOnClickListener {
                    val intent = android.content.Intent(this, ImagePreviewActivity::class.java).apply {
                        putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, post.imageUrl)
                    }
                    startActivity(intent)
                }
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Shared post
            if (!post.originalPostId.isNullOrEmpty()) {
                binding.cardSharedPost.visibility = View.VISIBLE
                binding.tvSharedAuthorName.text = post.originalAuthorName ?: "Unknown"
                binding.tvSharedContent.text = post.originalContent ?: ""

                if (!post.originalAuthorAvatarUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(post.originalAuthorAvatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.ivSharedAuthorAvatar)
                } else {
                    binding.ivSharedAuthorAvatar.setImageResource(R.drawable.ic_person)
                }

                if (!post.originalImageUrl.isNullOrEmpty()) {
                    binding.ivSharedImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(post.originalImageUrl)
                        .into(binding.ivSharedImage)

                    binding.ivSharedImage.setOnClickListener {
                         val intent = android.content.Intent(this, ImagePreviewActivity::class.java).apply {
                            putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, post.originalImageUrl)
                        }
                        startActivity(intent)
                    }
                } else {
                    binding.ivSharedImage.visibility = View.GONE
                }
            } else {
                binding.cardSharedPost.visibility = View.GONE
            }

            // Update like UI
            updateLikeUI()
        }

        // Like button click
        binding.containerLike.setOnClickListener {
            toggleLike()
        }

        binding.containerShare.setOnClickListener {
            // Share logic
             post?.let { p ->
                val intent = android.content.Intent(this, CreatePostActivity::class.java).apply {
                    putExtra("EXTRA_SHARED_POST", p)
                }
                startActivity(intent)
             }
        }

        // Send comment button
        binding.btnSendComment.setOnClickListener {
            sendComment()
        }

        binding.btnCancelReply.setOnClickListener {
            cancelReplyMode()
        }

        // Updating the title padding for the comment activity
        binding.tvHeaderTitle.setPadding(16, 16, 16, 16) // Adjust padding values as needed
    }

    private fun cancelReplyMode() {
        replyingToComment = null
        binding.replyIndicatorLayout.visibility = View.GONE
        binding.etComment.hint = "Write a comment..."
        binding.tvReplyingTo.text = ""
    }

    private fun updateCommentCountUI() {
        commentCount = comments.size
        binding.tvCommentCount.text = commentCount.toString()
    }

    private fun toggleLike() {
        val currentPost = post ?: return
        val currentUserId = UserPreferences.getUserId(this)

        // Toggle locally
        val newLikedBy = if (isLiked) {
             if (currentPost.likedBy.contains(currentUserId)) currentPost.likedBy - currentUserId else currentPost.likedBy
        } else {
             if (!currentPost.likedBy.contains(currentUserId)) currentPost.likedBy + currentUserId else currentPost.likedBy
        }

        isLiked = !isLiked
        likeCount = if (isLiked) likeCount + 1 else if (likeCount > 0) likeCount - 1 else 0
        updateLikeUI()

        val updatedPost = currentPost.copy(
            likedBy = newLikedBy,
            likeCount = likeCount
        )
        post = updatedPost

        // Send to server
        val senderName = UserPreferences.getUserName(this)
        val senderAvatar = UserPreferences.getUserAvatar(this)

        lifecycleScope.launch {
            val result = PostRepository.getInstance().toggleLikePost(currentPost.id, isLiked, senderName, senderAvatar)
            if (result.isFailure) {
                // Revert UI if needed, but for now just show error
                Toast.makeText(this@CommentsActivity, "Failed to update like", Toast.LENGTH_SHORT).show()
                // Revert local state
                isLiked = !isLiked
                likeCount = if (isLiked) likeCount + 1 else if (likeCount > 0) likeCount - 1 else 0
                updateLikeUI()
            }
        }
    }

    private fun updateLikeUI() {
        if (isLiked) {
            binding.ivLike.setImageResource(R.drawable.ic_heart)
            binding.ivLike.setColorFilter(ContextCompat.getColor(this, R.color.accent_pink))
            binding.tvLikeCount.setTextColor(ContextCompat.getColor(this, R.color.accent_pink))
        } else {
            binding.ivLike.setImageResource(R.drawable.ic_heart_outline)
            binding.ivLike.setColorFilter(ContextCompat.getColor(this, R.color.secondary_gray))
            binding.tvLikeCount.setTextColor(ContextCompat.getColor(this, R.color.secondary_gray))
        }
        binding.tvLikeCount.text = likeCount.toString()
    }

    private fun setupRecyclerView() {
        val currentUserId = UserPreferences.getUserId(this)
        // Ensure post is non-null before setting up adapter or handle null post appropriately
        val postOwnerId = post?.userId ?: ""

        commentAdapter = CommentAdapter(
            currentUserId = currentUserId,
            postOwnerId = postOwnerId,
            onLikeClick = { comment -> handleLikeComment(comment) },
            onDislikeClick = { comment -> handleDislikeComment(comment) },
            onReplyClick = { comment -> handleReplyComment(comment) },
            onDeleteCommentClick = { comment -> handleDeleteComment(comment) },
            onDeleteReplyClick = { parentComment, reply -> handleDeleteReply(parentComment, reply) },
            onLikeReplyClick = { parentComment, reply -> handleLikeReply(parentComment, reply) },
            onDislikeReplyClick = { parentComment, reply -> handleDislikeReply(parentComment, reply) }
        )
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentAdapter
        }
    }

    private fun handleDeleteComment(comment: Comment) {
        val postId = post?.id ?: return

        // Confirm dialog or just delete
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                 lifecycleScope.launch {
                     val result = PostRepository.getInstance().deleteComment(postId, comment.id)
                     if (result.isSuccess) {
                         comments.remove(comment)
                         commentAdapter.submitList(comments.toList())
                         updateCommentCountUI()
                         Toast.makeText(this@CommentsActivity, "Comment deleted", Toast.LENGTH_SHORT).show()
                     } else {
                         Toast.makeText(this@CommentsActivity, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                     }
                 }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDeleteReply(parentComment: Comment, reply: Comment) {
        val postId = post?.id ?: return

        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Reply")
            .setMessage("Are you sure you want to delete this reply?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val result = PostRepository.getInstance().deleteReply(postId, parentComment.id, reply)
                    if (result.isSuccess) {
                         // Find parent and update locally
                         val parentIndex = comments.indexOfFirst { it.id == parentComment.id }
                         if (parentIndex != -1) {
                             val parent = comments[parentIndex]
                             val updatedReplies = parent.replies.filter { it.id != reply.id }
                             comments[parentIndex] = parent.copy(replies = updatedReplies)
                             commentAdapter.submitList(comments.toList())
                             commentAdapter.notifyItemChanged(parentIndex)
                         }
                         Toast.makeText(this@CommentsActivity, "Reply deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CommentsActivity, "Failed to delete reply", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleLikeComment(comment: Comment) {
        val currentUserId = UserPreferences.getUserId(this)
        val postId = post?.id ?: return

        // Optimistic update
        val index = comments.indexOfFirst { it.id == comment.id }
        if (index != -1) {
            val isLiked = comment.likedBy.contains(currentUserId)
            val isDisliked = comment.dislikedBy.contains(currentUserId)

            val newLikedBy = if (isLiked) comment.likedBy - currentUserId else comment.likedBy + currentUserId
            val newLikesCount = if (isLiked) kotlin.math.max(0, comment.likesCount - 1) else comment.likesCount + 1

            // If liking, remove dislike
            val newDislikedBy = if (!isLiked && isDisliked) comment.dislikedBy - currentUserId else comment.dislikedBy
            val newDislikesCount = if (!isLiked && isDisliked) kotlin.math.max(0, comment.dislikesCount - 1) else comment.dislikesCount

            val updatedComment = comment.copy(
                likedBy = newLikedBy,
                likesCount = newLikesCount,
                dislikedBy = newDislikedBy,
                dislikesCount = newDislikesCount
            )

            comments[index] = updatedComment
            commentAdapter.submitList(comments.toList())

            // Get user info
            val senderName = UserPreferences.getUserName(this@CommentsActivity)
            val senderAvatar = UserPreferences.getUserAvatar(this@CommentsActivity)

            // Call Repo
            lifecycleScope.launch {
                val result = PostRepository.getInstance().toggleLikeComment(postId, comment.id, currentUserId, senderName, senderAvatar)
                if (result.isFailure) {
                    // Revert
                    if (index < comments.size) {
                        comments[index] = comment
                        commentAdapter.submitList(comments.toList())
                    }
                    Toast.makeText(this@CommentsActivity, "Failed to like", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleDislikeComment(comment: Comment) {
        val currentUserId = UserPreferences.getUserId(this)
        val postId = post?.id ?: return

        // Optimistic update
        val index = comments.indexOfFirst { it.id == comment.id }
        if (index != -1) {
            val isDisliked = comment.dislikedBy.contains(currentUserId)
            val isLiked = comment.likedBy.contains(currentUserId)

            val newDislikedBy = if (isDisliked) comment.dislikedBy - currentUserId else comment.dislikedBy + currentUserId
            val newDislikesCount = if (isDisliked) kotlin.math.max(0, comment.dislikesCount - 1) else comment.dislikesCount + 1

            // If disliking, remove like
            val newLikedBy = if (!isDisliked && isLiked) comment.likedBy - currentUserId else comment.likedBy
            val newLikesCount = if (!isDisliked && isLiked) kotlin.math.max(0, comment.likesCount - 1) else comment.likesCount

            val updatedComment = comment.copy(
                likedBy = newLikedBy,
                likesCount = newLikesCount,
                dislikedBy = newDislikedBy,
                dislikesCount = newDislikesCount
            )

            comments[index] = updatedComment
            commentAdapter.submitList(comments.toList())

            // Get user info
            val senderName = UserPreferences.getUserName(this@CommentsActivity)
            val senderAvatar = UserPreferences.getUserAvatar(this@CommentsActivity)

            // Call Repo
            lifecycleScope.launch {
                val result = PostRepository.getInstance().toggleDislikeComment(postId, comment.id, currentUserId, senderName, senderAvatar)
                if (result.isFailure) {
                    // Revert
                    if (index < comments.size) {
                        comments[index] = comment
                        commentAdapter.submitList(comments.toList())
                    }
                    Toast.makeText(this@CommentsActivity, "Failed to dislike", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLikeReply(parentComment: Comment, reply: Comment) {
        val currentUserId = UserPreferences.getUserId(this)
        val postId = post?.id ?: return

        val parentIndex = comments.indexOfFirst { it.id == parentComment.id }
        if (parentIndex != -1) {
            val parent = comments[parentIndex]
            val replyIndex = parent.replies.indexOfFirst { it.id == reply.id }

            if (replyIndex != -1) {
                val replyToUpdate = parent.replies[replyIndex]
                val isLiked = replyToUpdate.likedBy.contains(currentUserId)
                val isDisliked = replyToUpdate.dislikedBy.contains(currentUserId)

                val newLikedBy = if (isLiked) replyToUpdate.likedBy - currentUserId else replyToUpdate.likedBy + currentUserId
                val newLikesCount = if (isLiked) kotlin.math.max(0, replyToUpdate.likesCount - 1) else replyToUpdate.likesCount + 1

                // If liking, remove dislike
                val newDislikedBy = if (!isLiked && isDisliked) replyToUpdate.dislikedBy - currentUserId else replyToUpdate.dislikedBy
                val newDislikesCount = if (!isLiked && isDisliked) kotlin.math.max(0, replyToUpdate.dislikesCount - 1) else replyToUpdate.dislikesCount

                val updatedReply = replyToUpdate.copy(
                    likedBy = newLikedBy,
                    likesCount = newLikesCount,
                    dislikedBy = newDislikedBy,
                    dislikesCount = newDislikesCount
                )

                val updatedReplies = parent.replies.toMutableList()
                updatedReplies[replyIndex] = updatedReply

                val updatedParent = parent.copy(replies = updatedReplies)
                comments[parentIndex] = updatedParent
                commentAdapter.submitList(comments.toList())
                commentAdapter.notifyItemChanged(parentIndex) // Force update for nested RV usually needs more work but ListAdapter should handle diff if object changed

                lifecycleScope.launch {
                    val result = PostRepository.getInstance().toggleLikeReply(postId, parentComment.id, reply.id, currentUserId)
                    if (result.isFailure) {
                        // Revert
                        if (parentIndex < comments.size) {
                            comments[parentIndex] = parent
                            commentAdapter.submitList(comments.toList())
                            commentAdapter.notifyItemChanged(parentIndex)
                        }
                        Toast.makeText(this@CommentsActivity, "Failed to like reply", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleDislikeReply(parentComment: Comment, reply: Comment) {
        val currentUserId = UserPreferences.getUserId(this)
        val postId = post?.id ?: return

        val parentIndex = comments.indexOfFirst { it.id == parentComment.id }
        if (parentIndex != -1) {
            val parent = comments[parentIndex]
            val replyIndex = parent.replies.indexOfFirst { it.id == reply.id }

            if (replyIndex != -1) {
                val replyToUpdate = parent.replies[replyIndex]
                val isLiked = replyToUpdate.likedBy.contains(currentUserId)
                val isDisliked = replyToUpdate.dislikedBy.contains(currentUserId)

                val newDislikedBy = if (isDisliked) replyToUpdate.dislikedBy - currentUserId else replyToUpdate.dislikedBy + currentUserId
                val newDislikesCount = if (isDisliked) kotlin.math.max(0, replyToUpdate.dislikesCount - 1) else replyToUpdate.dislikesCount + 1

                // If disliking, remove like
                val newLikedBy = if (!isDisliked && isLiked) replyToUpdate.likedBy - currentUserId else replyToUpdate.likedBy
                val newLikesCount = if (!isDisliked && isLiked) kotlin.math.max(0, replyToUpdate.likesCount - 1) else replyToUpdate.likesCount

                val updatedReply = replyToUpdate.copy(
                    likedBy = newLikedBy,
                    likesCount = newLikesCount,
                    dislikedBy = newDislikedBy,
                    dislikesCount = newDislikesCount
                )

                val updatedReplies = parent.replies.toMutableList()
                updatedReplies[replyIndex] = updatedReply

                val updatedParent = parent.copy(replies = updatedReplies)
                comments[parentIndex] = updatedParent
                commentAdapter.submitList(comments.toList())
                commentAdapter.notifyItemChanged(parentIndex)

                lifecycleScope.launch {
                    val result = PostRepository.getInstance().toggleDislikeReply(postId, parentComment.id, reply.id, currentUserId)
                    if (result.isFailure) {
                         // Revert
                        if (parentIndex < comments.size) {
                            comments[parentIndex] = parent
                            commentAdapter.submitList(comments.toList())
                            commentAdapter.notifyItemChanged(parentIndex)
                        }
                        Toast.makeText(this@CommentsActivity, "Failed to dislike reply", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleReplyComment(comment: Comment) {
        replyingToComment = comment
        binding.replyIndicatorLayout.visibility = View.VISIBLE
        binding.tvReplyingTo.text = getString(R.string.replying_to_format, comment.authorName)

        // Add @mention to the edit text
        val mentionText = "@${comment.authorName} "
        binding.etComment.setText(mentionText)
        binding.etComment.setSelection(mentionText.length)

        binding.etComment.requestFocus()
        // Show keyboard
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun loadComments() {
        val currentPost = post ?: return
        if (currentPost.id.isEmpty()) {
            Toast.makeText(this, "Error: Post ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show loading or existing comments while fetching
        commentAdapter.submitList(comments.toList())
        updateCommentCountUI()

        // Fetch fresh comments from backend (Real-time)
        lifecycleScope.launch {
            try {
                PostRepository.getInstance().listenToComments(currentPost.id).collect { fetchedComments ->
                    comments.clear()
                    comments.addAll(fetchedComments)
                    commentAdapter.submitList(comments.toList())
                    updateCommentCountUI()
                }
            } catch (e: Exception) {
                if (comments.isEmpty()) {
                    Toast.makeText(this@CommentsActivity, "Failed to load comments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }

        // Fetch fresh post data (likes, shares) separately
        lifecycleScope.launch {
            val postResult = PostRepository.getInstance().getPost(currentPost.id)
            postResult.onSuccess { fetchedPost ->
                post = fetchedPost
                likeCount = fetchedPost.likeCount
                val currentUserId = UserPreferences.getUserId(this@CommentsActivity)
                isLiked = fetchedPost.likedBy.contains(currentUserId)

                // Update UI
                updateLikeUI()
                binding.tvShareCount.text = fetchedPost.shareCount.toString()
                // Comment count is handled by the real-time listener above
            }
        }
    }

    private fun sendComment() {
        val commentText = binding.etComment.text.toString().trim()

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        var userName = UserPreferences.getUserName(this)
        var userAvatar = UserPreferences.getUserAvatar(this)
        var userId = UserPreferences.getUserId(this)

        // Fallback to FirebaseAuth if local prefs are missing or default
        if (currentUser != null) {
            if (userId == "user_default" || userId.isEmpty()) {
                userId = currentUser.uid
            }
            if (userName == "You" || userName.isEmpty()) {
                userName = currentUser.displayName ?: "User"
            }
            if (userAvatar.isEmpty()) {
                userAvatar = currentUser.photoUrl?.toString() ?: ""
            }
        }

        val postId = post?.id ?: return

        // Disable button to prevent double-click
        binding.btnSendComment.isEnabled = false

        lifecycleScope.launch {
            try {
                if (replyingToComment != null) {
                    // Handle reply
                    val parentId = replyingToComment!!.id
                    val newReply = Comment(
                        postId = postId,
                        userId = userId,
                        authorName = userName,
                        authorAvatarUrl = userAvatar.ifEmpty { null },
                        content = commentText,
                        timestamp = System.currentTimeMillis()
                    )

                    val result = PostRepository.getInstance().replyToComment(postId, parentId, newReply)
                    if (result.isSuccess) {
                        // List update is handled by SnapshotListener
                        cancelReplyMode()
                        binding.etComment.text?.clear()
                        Toast.makeText(this@CommentsActivity, "Reply sent!", Toast.LENGTH_SHORT).show()
                        sendLocalNotification("Comment Reply", "You replied: $commentText")
                    } else {
                        Toast.makeText(this@CommentsActivity, "Failed to send reply: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle new top level comment
                    val newComment = Comment(
                        postId = postId,
                        userId = userId,
                        authorName = userName,
                        authorAvatarUrl = userAvatar.ifEmpty { null },
                        content = commentText,
                        timestamp = System.currentTimeMillis()
                    )

                    val result = PostRepository.getInstance().commentPost(postId, newComment)
                    if (result.isSuccess) {
                        // List update is handled by SnapshotListener
                        binding.etComment.text?.clear()
                        Toast.makeText(this@CommentsActivity, "Comment sent!", Toast.LENGTH_SHORT).show()
                        sendLocalNotification("New Comment", "You commented: $commentText")
                    } else {
                        Toast.makeText(this@CommentsActivity, "Failed to send comment: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@CommentsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.btnSendComment.isEnabled = true
            }
        }
    }


    private fun sendLocalNotification(title: String, message: String) {
        val channelId = "habit_tracker_comments"
        val notificationId = System.currentTimeMillis().toInt()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Comments",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(this, CommentsActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Re-open this post if clicked (simplified for now, ideally pass post ID)
            if (post != null) {
                putExtra(EXTRA_POST_ID, post?.id)
                putExtra(EXTRA_POST_USER_ID, post?.userId)
                 putExtra(EXTRA_AUTHOR_NAME, post?.authorName)
                 // Add other extras if needed to fully restore state
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this resource exists or use R.drawable.ic_notification if available
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ requires POST_NOTIFICATIONS permission
            e.printStackTrace()
        }
    }

    private fun returnResult() {
        val resultIntent = android.content.Intent().apply {
            putExtra(RESULT_POST_ID, post?.id)
            putExtra(RESULT_COMMENT_COUNT, commentCount)
            putExtra(RESULT_LIKE_COUNT, likeCount)
            putExtra(RESULT_IS_LIKED, isLiked)
            putParcelableArrayListExtra(RESULT_COMMENTS, ArrayList(comments))
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
        const val EXTRA_POST_USER_ID = "extra_post_user_id"
        const val EXTRA_AUTHOR_NAME = "extra_author_name"
        const val EXTRA_AUTHOR_AVATAR = "extra_author_avatar"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_LIKES_COUNT = "extra_likes_count"
        const val EXTRA_COMMENTS_COUNT = "extra_comments_count"
        const val EXTRA_IS_LIKED = "extra_is_liked"
        const val EXTRA_COMMENTS = "extra_comments"

        const val EXTRA_ORIGINAL_POST_ID = "extra_original_post_id"
        const val EXTRA_ORIGINAL_USER_ID = "extra_original_user_id"
        const val EXTRA_ORIGINAL_AUTHOR_NAME = "extra_original_author_name"
        const val EXTRA_ORIGINAL_AUTHOR_AVATAR = "extra_original_author_avatar"
        const val EXTRA_ORIGINAL_CONTENT = "extra_original_content"
        const val EXTRA_ORIGINAL_IMAGE_URL = "extra_original_image_url"

        const val RESULT_POST_ID = "result_post_id"
        const val RESULT_COMMENT_COUNT = "result_comment_count"
        const val RESULT_LIKE_COUNT = "result_like_count"
        const val RESULT_IS_LIKED = "result_is_liked"
        const val RESULT_COMMENTS = "result_comments"
    }
}
