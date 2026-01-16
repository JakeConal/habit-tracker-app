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
import com.example.habittracker.data.model.Comment
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(
    private val currentUserId: String,
    private val postOwnerId: String,
    private val onLikeClick: (Comment) -> Unit,
    private val onDislikeClick: (Comment) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val onDeleteCommentClick: (Comment) -> Unit,
    private val onDeleteReplyClick: (Comment, Comment) -> Unit, // parentComment, reply
    private val onLikeReplyClick: (Comment, Comment) -> Unit, // parentComment, reply
    private val onDislikeReplyClick: (Comment, Comment) -> Unit // parentComment, reply
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            currentUserId,
            postOwnerId,
            onLikeClick,
            onDislikeClick,
            onReplyClick,
            onDeleteCommentClick,
            onDeleteReplyClick,
            onLikeReplyClick,
            onDislikeReplyClick
        )
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCommentAuthorAvatar: ShapeableImageView = itemView.findViewById(R.id.ivCommentAuthorAvatar)
        private val tvCommentAuthorName: TextView = itemView.findViewById(R.id.tvCommentAuthorName)
        private val tvCommentTimestamp: TextView = itemView.findViewById(R.id.tvCommentTimestamp)
        private val tvCommentContent: TextView = itemView.findViewById(R.id.tvCommentContent)

        private val containerCommentLike: View = itemView.findViewById(R.id.containerCommentLike)
        private val ivCommentLike: ImageView = itemView.findViewById(R.id.ivCommentLike)
        private val tvCommentLikeCount: TextView = itemView.findViewById(R.id.tvCommentLikeCount)

        private val containerCommentDislike: View = itemView.findViewById(R.id.containerCommentDislike)
        private val ivCommentDislike: ImageView = itemView.findViewById(R.id.ivCommentDislike)
        private val tvCommentDislikeCount: TextView = itemView.findViewById(R.id.tvCommentDislikeCount)

        private val btnReply: TextView = itemView.findViewById(R.id.btnReply)
        private val tvReplyCount: TextView = itemView.findViewById(R.id.tvReplyCount)
        private val rvReplies: RecyclerView = itemView.findViewById(R.id.rvReplies)

        private val btnDeleteComment: TextView = itemView.findViewById(R.id.btnDeleteComment)

        fun bind(
            comment: Comment,
            currentUserId: String,
            postOwnerId: String,
            onLikeClick: (Comment) -> Unit,
            onDislikeClick: (Comment) -> Unit,
            onReplyClick: (Comment) -> Unit,
            onDeleteCommentClick: (Comment) -> Unit,
            onDeleteReplyClick: (Comment, Comment) -> Unit,
            onLikeReplyClick: (Comment, Comment) -> Unit,
            onDislikeReplyClick: (Comment, Comment) -> Unit
        ) {
            tvCommentAuthorName.text = comment.authorName

            // Format timestamp
            val date = Date(comment.timestamp)
            val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            tvCommentTimestamp.text = format.format(date)

            tvCommentContent.text = comment.content

            // Show features
            containerCommentLike.visibility = View.VISIBLE
            containerCommentDislike.visibility = View.VISIBLE
            btnReply.visibility = View.VISIBLE

            // Likes
            tvCommentLikeCount.text = if (comment.likesCount > 0) comment.likesCount.toString() else "0"
            val isLiked = comment.likedBy.contains(currentUserId)
            if (isLiked) {
                ivCommentLike.setImageResource(R.drawable.ic_heart)
                ivCommentLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                tvCommentLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_pink))
            } else {
                ivCommentLike.setImageResource(R.drawable.ic_heart_outline)
                ivCommentLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                tvCommentLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
            }

            containerCommentLike.setOnClickListener { onLikeClick(comment) }

            // Dislikes
            tvCommentDislikeCount.text = if (comment.dislikesCount > 0) comment.dislikesCount.toString() else "0"
            val isDisliked = comment.dislikedBy.contains(currentUserId)
            if (isDisliked) {
                ivCommentDislike.setImageResource(R.drawable.ic_thumb_down)
                ivCommentDislike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.primary_blue)) // Or another color
                tvCommentDislikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_blue))
            } else {
                ivCommentDislike.setImageResource(R.drawable.ic_thumb_down_outline)
                ivCommentDislike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                tvCommentDislikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
            }

            containerCommentDislike.setOnClickListener { onDislikeClick(comment) }

            // Reply
            btnReply.setOnClickListener { onReplyClick(comment) }

            // Delete permission: Comment author OR Post owner
            if (currentUserId == comment.userId || currentUserId == postOwnerId) {
                btnDeleteComment.visibility = View.VISIBLE
                btnDeleteComment.setOnClickListener { onDeleteCommentClick(comment) }
            } else {
                btnDeleteComment.visibility = View.GONE
            }

            // Replies
            if (comment.replies.isNotEmpty()) {
                rvReplies.visibility = View.VISIBLE
                tvReplyCount.visibility = View.VISIBLE
                tvReplyCount.text = itemView.context.getString(R.string.replies_count_format, comment.replies.size)

                // Collect potential names for @mention highlighting
                val participants = comment.replies.map { it.authorName }.toMutableSet()
                participants.add(comment.authorName)

                val replyAdapter = ReplyAdapter(
                    comment.replies,
                    currentUserId,
                    postOwnerId,
                    participants,
                    onDeleteReplyClick = { reply -> onDeleteReplyClick(comment, reply) },
                    onLikeReplyClick = { reply -> onLikeReplyClick(comment, reply) },
                    onDislikeReplyClick = { reply -> onDislikeReplyClick(comment, reply) },
                    onReplyToReplyClick = { _ -> onReplyClick(comment) } // Re-use main reply click to reply to parent
                )
                rvReplies.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(itemView.context)
                rvReplies.adapter = replyAdapter
            } else {
                rvReplies.visibility = View.GONE
                tvReplyCount.visibility = View.GONE
            }

            // Avatar
            if (!comment.authorAvatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(comment.authorAvatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivCommentAuthorAvatar)
            } else {
                ivCommentAuthorAvatar.setImageResource(R.drawable.ic_person)
            }
        }
    }

    class ReplyAdapter(
        private val replies: List<Comment>,
        private val currentUserId: String,
        private val postOwnerId: String,
        private val participants: Set<String>,
        private val onDeleteReplyClick: (Comment) -> Unit,
        private val onLikeReplyClick: (Comment) -> Unit,
        private val onDislikeReplyClick: (Comment) -> Unit,
        private val onReplyToReplyClick: (Comment) -> Unit
    ) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_reply, parent, false)
            return ReplyViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
            holder.bind(replies[position], currentUserId, postOwnerId, participants, onDeleteReplyClick, onLikeReplyClick, onDislikeReplyClick, onReplyToReplyClick)
        }

        override fun getItemCount() = replies.size

        class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvReplyAuthorName: TextView = itemView.findViewById(R.id.tvReplyAuthorName)
            private val ivReplyAuthorAvatar: ImageView = itemView.findViewById(R.id.ivReplyAuthorAvatar) // Added reference
            private val tvReplyTimestamp: TextView = itemView.findViewById(R.id.tvReplyTimestamp)
            private val tvReplyContent: TextView = itemView.findViewById(R.id.tvReplyContent)
            private val btnDeleteReply: TextView? = itemView.findViewById(R.id.btnDeleteReply)

            private val containerReplyLike: View = itemView.findViewById(R.id.containerReplyLike)
            private val ivReplyLike: ImageView = itemView.findViewById(R.id.ivReplyLike)
            private val tvReplyLikeCount: TextView = itemView.findViewById(R.id.tvReplyLikeCount)

            private val containerReplyDislike: View = itemView.findViewById(R.id.containerReplyDislike)
            private val ivReplyDislike: ImageView = itemView.findViewById(R.id.ivReplyDislike)
            private val tvReplyDislikeCount: TextView = itemView.findViewById(R.id.tvReplyDislikeCount)

            private val btnReplyToReply: TextView = itemView.findViewById(R.id.btnReplyToReply)

            fun bind(
                reply: Comment,
                currentUserId: String,
                postOwnerId: String,
                participants: Set<String>,
                onDeleteReplyClick: (Comment) -> Unit,
                onLikeReplyClick: (Comment) -> Unit,
                onDislikeReplyClick: (Comment) -> Unit,
                onReplyToReplyClick: (Comment) -> Unit
            ) {
                // Avatar binding
                if (!reply.authorAvatarUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(reply.authorAvatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivReplyAuthorAvatar)
                } else {
                    ivReplyAuthorAvatar.setImageResource(R.drawable.ic_person)
                }

                tvReplyAuthorName.text = reply.authorName
                val date = Date(reply.timestamp)
                val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                tvReplyTimestamp.text = format.format(date)


                // Highlight @username
                val content = reply.content
                if (content.startsWith("@")) {
                    var matchName = ""
                    // Try to find a participant name that matches the start of content
                    // Sort by length descending to match longest name first
                    val sortedParticipants = participants.sortedByDescending { it.length }

                    for (name in sortedParticipants) {
                        if (content.startsWith("@$name")) {
                            matchName = name
                            break
                        }
                    }

                    val endIndex = if (matchName.isNotEmpty()) {
                        matchName.length + 1 // +1 for @
                    } else {
                        // Fallback to first space if no known participant matched
                        val splitIndex = content.indexOf(" ")
                        if (splitIndex != -1) splitIndex else 0
                    }

                    if (endIndex > 0) {
                        val spannable = android.text.SpannableString(content)
                        spannable.setSpan(
                            android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(itemView.context, R.color.primary_blue)
                            ),
                            0,
                            endIndex,
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        spannable.setSpan(
                            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                            0,
                            endIndex,
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        tvReplyContent.text = spannable
                    } else {
                        tvReplyContent.text = content
                    }
                } else {
                    tvReplyContent.text = content
                }

                // Like Logic
                val isLiked = reply.likedBy.contains(currentUserId)
                tvReplyLikeCount.text = reply.likesCount.toString()

                if (isLiked) {
                    ivReplyLike.setImageResource(R.drawable.ic_heart)
                    ivReplyLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                    tvReplyLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_pink))
                } else {
                    ivReplyLike.setImageResource(R.drawable.ic_heart_outline)
                    ivReplyLike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                    tvReplyLikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                }

                containerReplyLike.setOnClickListener { onLikeReplyClick(reply) }

                // Dislike Logic
                val isDisliked = reply.dislikedBy.contains(currentUserId)
                tvReplyDislikeCount.text = reply.dislikesCount.toString()

                if (isDisliked) {
                    ivReplyDislike.setImageResource(R.drawable.ic_thumb_down)
                    ivReplyDislike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.primary_blue))
                    tvReplyDislikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_blue))
                } else {
                    ivReplyDislike.setImageResource(R.drawable.ic_thumb_down_outline)
                    ivReplyDislike.setColorFilter(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                    tvReplyDislikeCount.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_gray))
                }

                containerReplyDislike.setOnClickListener { onDislikeReplyClick(reply) }

                // Reply to Reply
                btnReplyToReply.setOnClickListener { onReplyToReplyClick(reply) }

                // Delete
                if (currentUserId == reply.userId || currentUserId == postOwnerId) {
                    btnDeleteReply?.visibility = View.VISIBLE
                    btnDeleteReply?.setOnClickListener { onDeleteReplyClick(reply) }
                } else {
                    btnDeleteReply?.visibility = View.GONE
                }
            }
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
