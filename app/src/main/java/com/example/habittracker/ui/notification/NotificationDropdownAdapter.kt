package com.example.habittracker.ui.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Notification
import com.example.habittracker.databinding.ItemNotificationDropdownBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationDropdownAdapter(
    private val onNotificationClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationDropdownAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationDropdownBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationDropdownBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        fun bind(notification: Notification) {
            binding.apply {
                tvNotificationSender.text = notification.senderName
                tvNotificationAction.text = getActionText(notification)
                tvNotificationTime.text = timeFormat.format(Date(notification.timestamp))

                unreadDot.visibility = if (notification.read) View.GONE else View.VISIBLE

                if (notification.senderAvatarUrl.isNotEmpty()) {
                    Glide.with(ivSenderAvatar.context)
                        .load(notification.senderAvatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivSenderAvatar)
                } else {
                    ivSenderAvatar.setImageResource(R.drawable.ic_person)
                }

                root.setOnClickListener {
                    onNotificationClick(notification)
                }
            }
        }

        private fun getActionText(notification: Notification): String {
            return when (notification.type) {
                Notification.NotificationType.LIKE_POST -> "liked your post"
                Notification.NotificationType.COMMENT_POST -> "commented on your post"
                Notification.NotificationType.SHARE_POST -> "shared your post"
                Notification.NotificationType.REPLY_COMMENT -> "replied to your comment"
                Notification.NotificationType.LIKE_COMMENT -> "liked your comment"
                Notification.NotificationType.DISLIKE_COMMENT -> "disliked your comment"
            }
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}
