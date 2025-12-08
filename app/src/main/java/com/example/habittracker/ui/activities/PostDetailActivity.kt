package com.example.habittracker.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.models.Post
import com.example.habittracker.ui.adapters.CommentAdapter
import com.example.habittracker.ui.viewmodels.FeedViewModel
import com.example.habittracker.ui.viewmodels.PostDetailViewModel
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {

    private val feedViewModel: FeedViewModel by viewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val postId = intent.getStringExtra("POST_ID") ?: return

        feedViewModel.posts.observe(this) { posts ->
            currentPost = posts.find { it.id == postId }
            currentPost?.let {
                bindPostData(it)
            }
        }

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        rvComments.layoutManager = LinearLayoutManager(this)

        val commentInput = findViewById<EditText>(R.id.commentInput)
        val submitCommentButton = findViewById<Button>(R.id.submitCommentButton)

        lifecycleScope.launch {
            postDetailViewModel.comments.collect { comments ->
                commentAdapter = CommentAdapter(comments)
                rvComments.adapter = commentAdapter
            }
        }

        submitCommentButton.setOnClickListener {
            val commentText = commentInput.text.toString()
            if (commentText.isNotBlank()) {
                postDetailViewModel.addComment(postId, commentText)
                commentInput.text.clear()
            }
        }

        postDetailViewModel.loadComments(postId)
    }

    private fun bindPostData(post: Post) {
        val postLayout = findViewById<View>(R.id.postLayout)
        val ivAvatar: ShapeableImageView = postLayout.findViewById(R.id.ivAvatar)
        val tvAuthorName: TextView = postLayout.findViewById(R.id.tvAuthorName)
        val tvTimestamp: TextView = postLayout.findViewById(R.id.tvTimestamp)
        val tvContent: TextView = postLayout.findViewById(R.id.tvContent)
        val ivPostImage: ShapeableImageView = postLayout.findViewById(R.id.ivPostImage)
        val layoutLike: LinearLayout = postLayout.findViewById(R.id.layoutLike)
        val ivLike: ImageView = postLayout.findViewById(R.id.ivLike)
        val tvStats: TextView = postLayout.findViewById(R.id.tvStats)

        tvAuthorName.text = post.author.name
        tvTimestamp.text = "2 hours ago" // Replace with actual timestamp logic
        tvContent.text = post.content

        if (!post.author.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(post.author.avatarUrl)
                .placeholder(R.drawable.bg_avatar_placeholder)
                .circleCrop()
                .into(ivAvatar)
        } else {
            ivAvatar.setImageResource(R.drawable.ic_profile)
        }

        if (!post.imageUrl.isNullOrEmpty()) {
            ivPostImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(post.imageUrl)
                .placeholder(R.color.secondary_gray)
                .centerCrop()
                .into(ivPostImage)
        } else {
            ivPostImage.visibility = View.GONE
        }

        if (post.isLiked) {
            ivLike.setImageResource(R.drawable.ic_heart)
            ivLike.setColorFilter(ContextCompat.getColor(this, R.color.accent_red))
        } else {
            ivLike.setImageResource(R.drawable.ic_heart_outline)
            ivLike.colorFilter = null
        }

        tvStats.text = "${post.likes} likes • ${post.comments} comments"

        layoutLike.setOnClickListener {
            feedViewModel.toggleLike(post.id)
        }
    }
}
