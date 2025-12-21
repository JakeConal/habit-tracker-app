package com.example.habittracker.ui.social.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Post
import com.example.habittracker.databinding.ItemPostBinding

class FeedAdapter(
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : ListAdapter<Post, FeedAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                tvAuthorName.text = post.authorName
                tvContent.text = post.content
                tvTimestamp.text = itemView.context.getString(R.string.time_ago_format, "2h") // TODO: Implement real timestamp formatting
                tvLikeCount.text = post.likeCount.toString()
                tvCommentCount.text = post.commentCount.toString()

                // Load avatar
                if (post.authorAvatarUrl != null) {
                    Glide.with(itemView.context)
                        .load(post.authorAvatarUrl)
                        .circleCrop()
                        .into(ivAuthorAvatar)
                } else {
                    ivAuthorAvatar.setImageResource(R.drawable.ic_person) // Placeholder
                }

                // Load post image if available
                if (post.imageUrl != null) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(post.imageUrl)
                        .centerCrop()
                        .into(ivPostImage)
                } else {
                    ivPostImage.visibility = View.GONE
                }

                // Click listeners
                ivLike.setOnClickListener { onLikeClick(post) }
                ivComment.setOnClickListener { onCommentClick(post) }
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
