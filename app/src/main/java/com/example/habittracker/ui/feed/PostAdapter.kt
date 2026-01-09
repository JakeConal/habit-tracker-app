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
import com.google.android.material.imageview.ShapeableImageView

class PostAdapter(
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onMoreOptionsClick: (Post, View) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), onLikeClick, onCommentClick, onMoreOptionsClick)
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
            onLikeClick: (Post) -> Unit,
            onCommentClick: (Post) -> Unit,
            onMoreOptionsClick: (Post, View) -> Unit
        ) {
            tvAuthorName.text = post.authorName
            tvTimestamp.text = post.timestamp
            tvContent.text = post.content
            tvLikeCount.text = post.likesCount.toString()
            tvCommentCount.text = post.commentsCount.toString()

            // Avatar
            if (post.authorAvatar.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(post.authorAvatar)
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
            if (post.isLiked) {
                ivLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                ivLike.setImageResource(R.drawable.ic_heart)
            } else {
                ivLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                ivLike.setImageResource(R.drawable.ic_heart_outline)
            }

            // Click listeners
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
