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
    private val onShareClick: (Post) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onMoreOptionsClick: (Post, View) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserId, onLikeClick, onCommentClick, onShareClick, onAuthorClick, onMoreOptionsClick)
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

        fun bind(
            post: Post,
            currentUserId: String,
            onLikeClick: (Post) -> Unit,
            onCommentClick: (Post) -> Unit,
            onShareClick: (Post) -> Unit,
            onAuthorClick: (String) -> Unit,
            onMoreOptionsClick: (Post, View) -> Unit
        ) {
            // Handle Shared Post display
            if (!post.originalPostId.isNullOrEmpty()) {
                 // Format: "A shared the post of B"
                 val sharedText = "${post.authorName} shared the post of ${post.originalAuthorName}"
                 tvAuthorName.text = sharedText

                 // Show sharer's content if any
                 if (post.content.isEmpty()) {
                     tvContent.visibility = View.GONE
                 } else {
                     tvContent.visibility = View.VISIBLE
                     tvContent.text = post.content
                 }

                 // Show Shared Card content
                 cardSharedPost?.visibility = View.VISIBLE
                 tvSharedAuthorName?.text = post.originalAuthorName

                 // Show Shared Author Avatar
                 if (!post.originalAuthorAvatarUrl.isNullOrEmpty() && ivSharedAuthorAvatar != null) {
                    Glide.with(itemView.context)
                        .load(post.originalAuthorAvatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivSharedAuthorAvatar)
                 } else {
                     ivSharedAuthorAvatar?.setImageResource(R.drawable.ic_person)
                 }

                 if (post.originalContent.isNullOrEmpty()) {
                     tvSharedContent?.visibility = View.GONE
                 } else {
                     tvSharedContent?.visibility = View.VISIBLE
                     tvSharedContent?.text = post.originalContent
                 }

                 if (!post.originalImageUrl.isNullOrEmpty() && ivSharedImage != null) {
                     ivSharedImage.visibility = View.VISIBLE
                     Glide.with(itemView.context)
                        .load(post.originalImageUrl)
                        .into(ivSharedImage)
                 } else {
                     ivSharedImage?.visibility = View.GONE
                 }

                 // Hide regular post image since we show shared content
                 ivPostImage.visibility = View.GONE

            } else {
                 // Normal post
                 cardSharedPost?.visibility = View.GONE

                 tvAuthorName.text = post.authorName
                 tvContent.visibility = View.VISIBLE
                 tvContent.text = post.content

                 // Handle post image visibility
                 if (!post.imageUrl.isNullOrEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(post.imageUrl)
                        .into(ivPostImage)
                } else {
                    ivPostImage.visibility = View.GONE
                }
            }

            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(post.timestamp))

            tvLikeCount.text = post.likeCount.toString()
            tvCommentCount.text = post.commentCount.toString()
            tvShareCount?.text = post.shareCount.toString()

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
            containerShare?.setOnClickListener { onShareClick(post) }

            // For shared posts, clicking properly should navigate to original post if requested?
            // User requested: "When anyone clicks on B's post... be taken to original post of B"
            // Since this IS a post in the feed (A shared B), clicking comment usually goes to details of THIS post (A's share).
            // But if they clicked on the "content" part which represents B's post, it should go to B.
            // However, the prompt says "click into the post of B".
            // If I am viewing "A shared B", and I click it, I usually go to A's post details.
            // If I click "B's content", I go to B.
            // But to keep it simple as per request: "click into the post of B" -> likely means interacting with the shared content.

            // Let's make the POST LISTENER handle the redirection based on originalPostId
            containerComment.setOnClickListener {
                // If it is a shared post, we might want to comment on the SHARE or the ORIGINAL?
                // Usually comment on the SHARE.
                // "When anyone clicks on B's post... be taken to original post of B"
                // If "A shared B", visually we see B's content.
                // If the user taps the image or content, they probably expect to go to B's original post.
                // If they tap "Comment" button, they expect to comment on A's share.

                // Let's adhere to standard behavior for buttons, but maybe add a click listener on the whole VIEW or Image/Content to go to original.
                onCommentClick(post)
            }

            // Add click listener for content to go to original post if it is a shared post
            if (!post.originalPostId.isNullOrEmpty()) {
                val originalClick = View.OnClickListener {
                     // We need to notify parent to navigate to ORIGINAL post
                     // We can reuse onCommentClick but we need to pass the ORIGINAL post data conceptually.
                     // But onCommentClick expects a Post object.
                     // We can construct a dummy Post object for the original post or handle it in Fragment.
                     // Let's pass the current post, and let Fragment decide.
                     // But Fragment doesn't know WHICH part was clicked (features or content).

                     // Actually, if I click the IMAGE of a shared post, I should probably go to the ORIGINAL post details.
                     // Implementing a specific callback for "Original Post Click" would be cleanest.
                     // But I can't change the constructor easily without affecting calls.
                     // For now, let's assume `onCommentClick` opens details.
                     // If I want to open ORIGINAL details, I should maybe pass a modified post to onCommentClick?
                     val originalPostSimulator = post.copy(
                         id = post.originalPostId!!,
                         userId = post.originalUserId ?: "",
                         authorName = post.originalAuthorName ?: "",
                         authorAvatarUrl = post.originalAuthorAvatarUrl,
                         content = post.originalContent ?: "",
                         imageUrl = post.originalImageUrl,
                         // Reset these for the simulated original post so it doesn't look like a share of itself
                         originalPostId = null,
                         originalUserId = null,
                         originalAuthorName = null,
                         originalAuthorAvatarUrl = null,
                         originalContent = null,
                         originalImageUrl = null
                     )
                     onCommentClick(originalPostSimulator)
                }
                ivPostImage.setOnClickListener(originalClick)
                tvContent.setOnClickListener(originalClick)

                // Also bind click to shared content views
                cardSharedPost?.setOnClickListener(originalClick)
                tvSharedAuthorName?.setOnClickListener(originalClick)
                ivSharedAuthorAvatar?.setOnClickListener(originalClick)
                tvSharedContent?.setOnClickListener(originalClick)
                ivSharedImage?.setOnClickListener(originalClick)

            } else {
                 // unexpected click behavior for normal posts?
                 // Standard behavior: click image -> full screen image or post details.
                 // let's bind simple details click
                 val detailsClick = View.OnClickListener { onCommentClick(post) }
                 ivPostImage.setOnClickListener(detailsClick)
                 tvContent.setOnClickListener(detailsClick)
            }

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
