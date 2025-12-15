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

class CommentReplyAdapter(
    private val onLikeClick: (CommentReply) -> Unit,
    private val onReplyClick: (CommentReply) -> Unit
) : ListAdapter<CommentReply, CommentReplyAdapter.ReplyViewHolder>(ReplyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(getItem(position), onLikeClick, onReplyClick)
    }

    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivReplyAuthorAvatar: ShapeableImageView = itemView.findViewById(R.id.ivReplyAuthorAvatar)
        private val tvReplyAuthorName: TextView = itemView.findViewById(R.id.tvReplyAuthorName)
        private val tvReplyTimestamp: TextView = itemView.findViewById(R.id.tvReplyTimestamp)
        private val tvReplyContent: TextView = itemView.findViewById(R.id.tvReplyContent)
        private val containerReplyLike: View = itemView.findViewById(R.id.containerReplyLike)
        private val ivReplyLike: ImageView = itemView.findViewById(R.id.ivReplyLike)
        private val tvReplyLikeCount: TextView = itemView.findViewById(R.id.tvReplyLikeCount)
        private val btnReplyToReply: TextView = itemView.findViewById(R.id.btnReplyToReply)
        private val tvNestedReplyCount: TextView = itemView.findViewById(R.id.tvNestedReplyCount)
        private val rvNestedReplies: RecyclerView = itemView.findViewById(R.id.rvNestedReplies)

        private var nestedReplyAdapter: CommentReplyAdapter? = null

        fun bind(
            reply: CommentReply,
            onLikeClick: (CommentReply) -> Unit,
            onReplyClick: (CommentReply) -> Unit
        ) {
            tvReplyAuthorName.text = reply.authorName
            tvReplyTimestamp.text = reply.timestamp
            tvReplyContent.text = reply.content
            tvReplyLikeCount.text = reply.likesCount.toString()

            // Avatar
            if (reply.authorAvatar.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(reply.authorAvatar)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivReplyAuthorAvatar)
            } else {
                ivReplyAuthorAvatar.setImageResource(R.drawable.ic_person)
            }

            // Like status
            if (reply.isLiked) {
                ivReplyLike.setImageResource(R.drawable.ic_heart)
                ivReplyLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                tvReplyLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_pink))
            } else {
                ivReplyLike.setImageResource(R.drawable.ic_heart_outline)
                ivReplyLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                tvReplyLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
            }

            // Nested replies
            if (reply.replies.isNotEmpty()) {
                tvNestedReplyCount.visibility = View.VISIBLE
                tvNestedReplyCount.text = "(${reply.replies.size})"
                rvNestedReplies.visibility = View.VISIBLE

                // Initialize nested adapter if needed
                if (nestedReplyAdapter == null) {
                    nestedReplyAdapter = CommentReplyAdapter(onLikeClick, onReplyClick)
                    rvNestedReplies.apply {
                        layoutManager = LinearLayoutManager(itemView.context)
                        adapter = nestedReplyAdapter
                    }
                }
                nestedReplyAdapter?.submitList(reply.replies)
            } else {
                tvNestedReplyCount.visibility = View.GONE
                rvNestedReplies.visibility = View.GONE
            }

            // Click listeners
            containerReplyLike.setOnClickListener { onLikeClick(reply) }
            btnReplyToReply.setOnClickListener { onReplyClick(reply) }
        }
    }

    class ReplyDiffCallback : DiffUtil.ItemCallback<CommentReply>() {
        override fun areItemsTheSame(oldItem: CommentReply, newItem: CommentReply): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommentReply, newItem: CommentReply): Boolean {
            return oldItem == newItem
        }
    }
}

