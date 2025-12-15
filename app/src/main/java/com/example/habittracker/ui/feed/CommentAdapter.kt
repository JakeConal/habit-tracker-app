package com.example.habittracker.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.google.android.material.imageview.ShapeableImageView

class CommentAdapter(
    private val onLikeClick: (Comment) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val onReplyLikeClick: (Comment, CommentReply) -> Unit,
    private val onReplyToReply: (Comment, CommentReply) -> Unit
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position), onLikeClick, onReplyClick, onReplyLikeClick, onReplyToReply)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCommentAuthorAvatar: ShapeableImageView = itemView.findViewById(R.id.ivCommentAuthorAvatar)
        private val tvCommentAuthorName: TextView = itemView.findViewById(R.id.tvCommentAuthorName)
        private val tvCommentTimestamp: TextView = itemView.findViewById(R.id.tvCommentTimestamp)
        private val tvCommentContent: TextView = itemView.findViewById(R.id.tvCommentContent)
        private val containerCommentLike: View = itemView.findViewById(R.id.containerCommentLike)
        private val ivCommentLike: ImageView = itemView.findViewById(R.id.ivCommentLike)
        private val tvCommentLikeCount: TextView = itemView.findViewById(R.id.tvCommentLikeCount)
        private val btnReply: TextView = itemView.findViewById(R.id.btnReply)
        private val tvReplyCount: TextView = itemView.findViewById(R.id.tvReplyCount)
        private val rvReplies: RecyclerView = itemView.findViewById(R.id.rvReplies)

        private val replyAdapter = CommentReplyAdapter(
            onLikeClick = { /* will be set in bind */ },
            onReplyClick = { /* will be set in bind */ }
        )

        private var currentComment: Comment? = null
        private var currentOnReplyLikeClick: ((Comment, CommentReply) -> Unit)? = null
        private var currentOnReplyToReply: ((Comment, CommentReply) -> Unit)? = null

        init {
            rvReplies.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = replyAdapter
            }
        }

        fun bind(
            comment: Comment,
            onLikeClick: (Comment) -> Unit,
            onReplyClick: (Comment) -> Unit,
            onReplyLikeClick: (Comment, CommentReply) -> Unit,
            onReplyToReply: (Comment, CommentReply) -> Unit
        ) {
            currentComment = comment
            currentOnReplyLikeClick = onReplyLikeClick
            currentOnReplyToReply = onReplyToReply

            tvCommentAuthorName.text = comment.authorName
            tvCommentTimestamp.text = comment.timestamp
            tvCommentContent.text = comment.content
            tvCommentLikeCount.text = comment.likesCount.toString()

            // Avatar
            if (comment.authorAvatar.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(comment.authorAvatar)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivCommentAuthorAvatar)
            } else {
                ivCommentAuthorAvatar.setImageResource(R.drawable.ic_person)
            }

            // Like status
            if (comment.isLiked) {
                ivCommentLike.setImageResource(R.drawable.ic_heart)
                ivCommentLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                tvCommentLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_pink))
            } else {
                ivCommentLike.setImageResource(R.drawable.ic_heart_outline)
                ivCommentLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                tvCommentLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
            }

            // Reply count
            if (comment.replies.isNotEmpty()) {
                tvReplyCount.visibility = View.VISIBLE
                tvReplyCount.text = "(${comment.replies.size})"
                rvReplies.visibility = View.VISIBLE

                // Create new adapter with callbacks for this comment
                val newReplyAdapter = CommentReplyAdapter(
                    onLikeClick = { reply ->
                        currentComment?.let { onReplyLikeClick(it, reply) }
                    },
                    onReplyClick = { reply ->
                        currentComment?.let { onReplyToReply(it, reply) }
                    }
                )
                rvReplies.adapter = newReplyAdapter
                newReplyAdapter.submitList(comment.replies)
            } else {
                tvReplyCount.visibility = View.GONE
                rvReplies.visibility = View.GONE
            }

            // Click listeners
            containerCommentLike.setOnClickListener { onLikeClick(comment) }
            btnReply.setOnClickListener { onReplyClick(comment) }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}

