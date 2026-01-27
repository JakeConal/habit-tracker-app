package com.example.habittracker.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
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
    private var votedChallengeIds: List<String> = emptyList(),
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onShareClick: (Post) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onVoteClick: (Post) -> Unit,
    private val onMoreOptionsClick: (Post, View) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserId, votedChallengeIds, onLikeClick, onCommentClick, onShareClick, onAuthorClick, onVoteClick, onMoreOptionsClick)
    }

    fun updateVotedChallengeIds(newIds: List<String>) {
        votedChallengeIds = newIds
        notifyDataSetChanged()
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
        private val containerShare: View? = itemView.findViewById(R.id.containerShare)
        private val tvShareCount: TextView? = itemView.findViewById(R.id.tvShareCount)

        // Shared post views
        private val cardSharedPost: View? = itemView.findViewById(R.id.cardSharedPost)
        private val tvSharedAuthorName: TextView? = itemView.findViewById(R.id.tvSharedAuthorName)
        private val ivSharedAuthorAvatar: ImageView? = itemView.findViewById(R.id.ivSharedAuthorAvatar)
        private val tvSharedContent: TextView? = itemView.findViewById(R.id.tvSharedContent)
        private val ivSharedImage: ImageView? = itemView.findViewById(R.id.ivSharedImage)

        // Challenge share views
        private val layoutChallengeShare: View? = itemView.findViewById(R.id.layoutChallengeShare)
        private val tvChallengeShareTitle: TextView? = itemView.findViewById(R.id.tvChallengeShareTitle)
        private val btnVoteChallenge: MaterialButton? = itemView.findViewById(R.id.btnVoteChallenge)

        fun bind(
            post: Post,
            currentUserId: String,
            votedChallengeIds: List<String>,
            onLikeClick: (Post) -> Unit,
            onCommentClick: (Post) -> Unit,
            onShareClick: (Post) -> Unit,
            onAuthorClick: (String) -> Unit,
            onVoteClick: (Post) -> Unit,
            onMoreOptionsClick: (Post, View) -> Unit
        ) {
            // Reset visibilities
            cardSharedPost?.visibility = View.GONE
            layoutChallengeShare?.visibility = View.GONE
            ivPostImage.visibility = View.GONE
            ivSharedImage?.visibility = View.GONE
            tvContent.visibility = View.VISIBLE
            ivSharedAuthorAvatar?.visibility = View.VISIBLE
            tvSharedAuthorName?.visibility = View.VISIBLE
            tvSharedContent?.visibility = View.VISIBLE

            val isVoted = post.votedBy.contains(currentUserId) || (post.challengeId != null && votedChallengeIds.contains(post.challengeId))

            if (!post.challengeId.isNullOrEmpty()) {
                // Challenge share (original OR reshared)
                cardSharedPost?.visibility = View.VISIBLE
                layoutChallengeShare?.visibility = View.VISIBLE
                tvChallengeShareTitle?.text = itemView.context.getString(R.string.challenge_title_format, post.challengeTitle)

                btnVoteChallenge?.apply {
                    if (isVoted) {
                        text = itemView.context.getString(R.string.voted)
                        isEnabled = false
                    } else {
                        text = itemView.context.getString(R.string.vote_count_format, post.voteCount)
                        isEnabled = true
                    }
                    setOnClickListener { onVoteClick(post) }
                }

                if (!post.originalPostId.isNullOrEmpty()) {
                    // Reshared challenge: show "A shared the post of B"
                    tvAuthorName.text = itemView.context.getString(R.string.shared_post_format, post.authorName, post.originalAuthorName)
                    tvContent.text = post.content
                    if (post.content.isEmpty()) tvContent.visibility = View.GONE

                    // Hide original author info inside card for challenges to keep it compact
                    ivSharedAuthorAvatar?.visibility = View.GONE
                    tvSharedAuthorName?.visibility = View.GONE
                    tvSharedContent?.visibility = View.GONE
                } else {
                    // Original challenge share
                    tvAuthorName.text = post.authorName
                    tvContent.text = post.content
                    ivSharedAuthorAvatar?.visibility = View.GONE
                    tvSharedAuthorName?.visibility = View.GONE
                    tvSharedContent?.visibility = View.GONE
                }

                // Show challenge image in the main image view (below the card)
                val challengeImageUrl = post.originalImageUrl ?: post.imageUrl
                if (!challengeImageUrl.isNullOrEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(challengeImageUrl).centerCrop().into(ivPostImage)
                }
            } else if (!post.originalPostId.isNullOrEmpty()) {
                // Normal shared post
                tvAuthorName.text = itemView.context.getString(R.string.shared_post_format, post.authorName, post.originalAuthorName)
                tvContent.text = post.content
                if (post.content.isEmpty()) tvContent.visibility = View.GONE

                cardSharedPost?.visibility = View.VISIBLE
                tvSharedAuthorName?.text = post.originalAuthorName
                tvSharedContent?.text = post.originalContent

                if (!post.originalAuthorAvatarUrl.isNullOrEmpty() && ivSharedAuthorAvatar != null) {
                    Glide.with(itemView.context).load(post.originalAuthorAvatarUrl).placeholder(R.drawable.ic_person).into(ivSharedAuthorAvatar)
                }

                // Show shared image INSIDE the card
                if (!post.originalImageUrl.isNullOrEmpty() && ivSharedImage != null) {
                    ivSharedImage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(post.originalImageUrl).centerCrop().into(ivSharedImage)
                }

                // If user attached a NEW image to their share, show it OUTSIDE the card
                if (!post.imageUrl.isNullOrEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(post.imageUrl).centerCrop().into(ivPostImage)
                }
            } else {
                // Normal post
                tvAuthorName.text = post.authorName
                tvContent.text = post.content
                if (!post.imageUrl.isNullOrEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(post.imageUrl).centerCrop().into(ivPostImage)
                }
            }

            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(post.timestamp))
            tvLikeCount.text = post.likeCount.toString()
            tvCommentCount.text = post.commentCount.toString()
            tvShareCount?.text = post.shareCount.toString()

            if (!post.authorAvatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(post.authorAvatarUrl).placeholder(R.drawable.ic_person).into(ivAuthorAvatar)
            } else {
                ivAuthorAvatar.setImageResource(R.drawable.ic_person)
            }

            val isLiked = post.likedBy.contains(currentUserId)
            ivLike.setColorFilter(ContextCompat.getColor(itemView.context, if (isLiked) R.color.accent_pink else R.color.secondary_gray))
            ivLike.setImageResource(if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline)

            ivAuthorAvatar.setOnClickListener { onAuthorClick(post.userId) }
            tvAuthorName.setOnClickListener { onAuthorClick(post.userId) }
            containerLike.setOnClickListener { onLikeClick(post) }
            containerShare?.setOnClickListener { onShareClick(post) }
            containerComment.setOnClickListener { onCommentClick(post) }
            ivMoreOptions.setOnClickListener { onMoreOptionsClick(post, ivMoreOptions) }
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
