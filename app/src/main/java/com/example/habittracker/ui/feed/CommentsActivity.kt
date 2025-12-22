package com.example.habittracker.ui.feed

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityCommentsBinding
import com.example.habittracker.utils.UserPreferences

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var commentAdapter: CommentAdapter
    private var post: Post? = null
    private var isLiked = false
    private var likeCount = 0
    private var commentCount = 0
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val timestamp = intent.getStringExtra(EXTRA_TIMESTAMP) ?: ""
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val likesCount = intent.getIntExtra(EXTRA_LIKES_COUNT, 0)
        val commentsCount = intent.getIntExtra(EXTRA_COMMENTS_COUNT, 0)
        val isLikedExtra = intent.getBooleanExtra(EXTRA_IS_LIKED, false)
        val existingComments = intent.getParcelableArrayListExtra<Comment>(EXTRA_COMMENTS) ?: arrayListOf()

        post = Post(
            id = postId,
            userId = "user_current", // Default user ID for comments activity
            authorName = authorName,
            authorAvatar = authorAvatar,
            timestamp = timestamp,
            content = content,
            imageUrl = imageUrl,
            likesCount = likesCount,
            commentsCount = commentsCount,
            isLiked = isLikedExtra,
            comments = existingComments
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

        // Display post data
        post?.let { post ->
            binding.tvAuthorName.text = post.authorName
            binding.tvTimestamp.text = post.timestamp
            binding.tvContent.text = post.content
            binding.tvLikeCount.text = post.likesCount.toString()

            // Avatar
            if (post.authorAvatar.isNotEmpty()) {
                Glide.with(this)
                    .load(post.authorAvatar)
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
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Update like UI
            updateLikeUI()
        }

        // Like button click
        binding.containerLike.setOnClickListener {
            toggleLike()
        }

        // Send comment button
        binding.btnSendComment.setOnClickListener {
            sendComment()
        }
    }

    private fun updateCommentCountUI() {
        commentCount = comments.size
        binding.tvCommentCount.text = commentCount.toString()
    }

    private fun toggleLike() {
        isLiked = !isLiked
        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
        updateLikeUI()

        // TODO: Send like status to server
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
        commentAdapter = CommentAdapter(
            onLikeClick = { comment ->
                toggleCommentLike(comment)
            },
            onReplyClick = { comment ->
                showReplyDialog(comment)
            },
            onReplyLikeClick = { comment, reply ->
                toggleReplyLike(comment, reply)
            },
            onReplyToReply = { comment, reply ->
                showReplyToReplyDialog(comment, reply)
            }
        )
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentAdapter
        }
    }

    private fun loadComments() {
        // Comments are already loaded from getPostFromIntent()
        // If no comments exist (shouldn't happen with our sample data), do nothing
        // The user can add new comments using the input field

        commentAdapter.submitList(comments.toList())
        updateCommentCountUI()
    }

    private fun sendComment() {
        val commentText = binding.etComment.text.toString().trim()

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user info from preferences
        val userName = UserPreferences.getUserName(this)
        val userAvatar = UserPreferences.getUserAvatar(this)

        // Create new comment
        val newComment = Comment(
            id = System.currentTimeMillis().toString(),
            authorName = userName,
            authorAvatar = userAvatar,
            timestamp = "Just now",
            content = commentText
        )

        // Add to the beginning of the list
        comments.add(0, newComment)

        // Update adapter with new list
        commentAdapter.submitList(comments.toList())

        // Update comment count UI
        updateCommentCountUI()

        // Clear input
        binding.etComment.text?.clear()

        // Show success message
        Toast.makeText(this, "Comment sent!", Toast.LENGTH_SHORT).show()

        // Scroll to top to show new comment
        binding.scrollView.post {
            binding.rvComments.scrollToPosition(0)
        }

        // TODO: Send comment to server and update the post's comment count in database
    }

    private fun toggleCommentLike(comment: Comment) {
        val index = comments.indexOfFirst { it.id == comment.id }
        if (index != -1) {
            val updatedComment = comment.copy(
                isLiked = !comment.isLiked,
                likesCount = if (comment.isLiked) comment.likesCount - 1 else comment.likesCount + 1
            )
            comments[index] = updatedComment
            commentAdapter.submitList(comments.toList())

            // TODO: Send like status to server
        }
    }

    private fun showReplyDialog(comment: Comment) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reply_comment, null)
        val etReply = dialogView.findViewById<android.widget.EditText>(R.id.etReply)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reply to ${comment.authorName}")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val replyText = etReply.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    addReplyToComment(comment, replyText)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Show keyboard
        etReply.requestFocus()
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(etReply, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun addReplyToComment(comment: Comment, replyText: String) {
        // Get user info from preferences
        val userName = UserPreferences.getUserName(this)
        val userAvatar = UserPreferences.getUserAvatar(this)

        val newReply = CommentReply(
            id = System.currentTimeMillis().toString(),
            authorName = userName,
            authorAvatar = userAvatar,
            timestamp = "Just now",
            content = replyText
        )

        val index = comments.indexOfFirst { it.id == comment.id }
        if (index != -1) {
            val updatedReplies = comment.replies.toMutableList()
            updatedReplies.add(newReply)

            val updatedComment = comment.copy(replies = updatedReplies)
            comments[index] = updatedComment
            commentAdapter.submitList(comments.toList())

            Toast.makeText(this, "Reply sent!", Toast.LENGTH_SHORT).show()

            // TODO: Send reply to server
        }
    }

    private fun toggleReplyLike(comment: Comment, reply: CommentReply) {
        val commentIndex = comments.indexOfFirst { it.id == comment.id }
        if (commentIndex != -1) {
            val updatedReplies = toggleReplyLikeRecursive(comment.replies, reply)
            val updatedComment = comment.copy(replies = updatedReplies)
            comments[commentIndex] = updatedComment
            commentAdapter.submitList(comments.toList())

            // TODO: Send like status to server
        }
    }

    private fun toggleReplyLikeRecursive(replies: List<CommentReply>, targetReply: CommentReply): List<CommentReply> {
        return replies.map { reply ->
            if (reply.id == targetReply.id) {
                reply.copy(
                    isLiked = !reply.isLiked,
                    likesCount = if (reply.isLiked) reply.likesCount - 1 else reply.likesCount + 1
                )
            } else if (reply.replies.isNotEmpty()) {
                reply.copy(replies = toggleReplyLikeRecursive(reply.replies, targetReply))
            } else {
                reply
            }
        }
    }

    private fun showReplyToReplyDialog(comment: Comment, parentReply: CommentReply) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reply_comment, null)
        val etReply = dialogView.findViewById<android.widget.EditText>(R.id.etReply)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reply to ${parentReply.authorName}")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val replyText = etReply.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    addNestedReply(comment, parentReply, replyText)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Show keyboard
        etReply.requestFocus()
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(etReply, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun addNestedReply(comment: Comment, parentReply: CommentReply, replyText: String) {
        // Get user info from preferences
        val userName = UserPreferences.getUserName(this)
        val userAvatar = UserPreferences.getUserAvatar(this)

        val newReply = CommentReply(
            id = System.currentTimeMillis().toString(),
            authorName = userName,
            authorAvatar = userAvatar,
            timestamp = "Just now",
            content = replyText
        )

        val commentIndex = comments.indexOfFirst { it.id == comment.id }
        if (commentIndex != -1) {
            val updatedReplies = addNestedReplyRecursive(comment.replies, parentReply, newReply)
            val updatedComment = comment.copy(replies = updatedReplies)
            comments[commentIndex] = updatedComment
            commentAdapter.submitList(comments.toList())

            Toast.makeText(this, "Reply sent!", Toast.LENGTH_SHORT).show()

            // TODO: Send reply to server
        }
    }

    private fun addNestedReplyRecursive(replies: List<CommentReply>, targetReply: CommentReply, newReply: CommentReply): List<CommentReply> {
        return replies.map { reply ->
            if (reply.id == targetReply.id) {
                val updatedNestedReplies = reply.replies.toMutableList()
                updatedNestedReplies.add(newReply)
                reply.copy(replies = updatedNestedReplies)
            } else if (reply.replies.isNotEmpty()) {
                reply.copy(replies = addNestedReplyRecursive(reply.replies, targetReply, newReply))
            } else {
                reply
            }
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
        const val EXTRA_AUTHOR_NAME = "extra_author_name"
        const val EXTRA_AUTHOR_AVATAR = "extra_author_avatar"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_LIKES_COUNT = "extra_likes_count"
        const val EXTRA_COMMENTS_COUNT = "extra_comments_count"
        const val EXTRA_IS_LIKED = "extra_is_liked"
        const val EXTRA_COMMENTS = "extra_comments"

        const val RESULT_POST_ID = "result_post_id"
        const val RESULT_COMMENT_COUNT = "result_comment_count"
        const val RESULT_LIKE_COUNT = "result_like_count"
        const val RESULT_IS_LIKED = "result_is_liked"
        const val RESULT_COMMENTS = "result_comments"
    }
}

