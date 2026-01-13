package com.example.habittracker.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Post
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(
    private val currentUserId: String,
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onMoreOptionsClick: (Post, View) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserId, onLikeClick, onCommentClick, onAuthorClick, onMoreOptionsClick)
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAuthorAvatar: ShapeableImageView = itemView.findViewById(R.id.ivAuthorAvatar)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        private val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        private val ivMoreOptions: ImageView = itemView.findViewById(R.id.ivMoreOptions)
        private val containerLike: View = itemView.findViewById(R.id.containerLike)
        private val containerComment: View = itemView.findViewById(R.id.containerComment)

        fun bind(
            post: Post,
            currentUserId: String,
            onLikeClick: (Post) -> Unit,
            onCommentClick: (Post) -> Unit,
            onAuthorClick: (String) -> Unit,
            onMoreOptionsClick: (Post, View) -> Unit
        ) {
            tvAuthorName.text = post.authorName

            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(post.timestamp))

            tvContent.text = post.content
            tvLikeCount.text = post.likeCount.toString()
            tvCommentCount.text = post.commentCount.toString()

            // Avatar
            if (!post.authorAvatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.authorAvatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivAuthorAvatar)
            } else {
                ivAuthorAvatar.setImageResource(R.drawable.ic_person)
            }

            // Handle post image visibility
            if (!post.imageUrl.isNullOrEmpty()) {
                ivPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .into(ivPostImage)
            } else {
                ivPostImage.visibility = View.GONE
            }

            // Like status
            val isLiked = post.likedBy.contains(currentUserId)
            if (isLiked) {
                ivLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                ivLike.setImageResource(R.drawable.ic_heart)
            } else {
                ivLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                ivLike.setImageResource(R.drawable.ic_heart_outline)
            }

            // Click listeners
            ivAuthorAvatar.setOnClickListener { onAuthorClick(post.userId) }
            tvAuthorName.setOnClickListener { onAuthorClick(post.userId) }
            containerLike.setOnClickListener { onLikeClick(post) }
            containerComment.setOnClickListener { onCommentClick(post) }

            ivMoreOptions.setOnClickListener {
                onMoreOptionsClick(post, ivMoreOptions)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
