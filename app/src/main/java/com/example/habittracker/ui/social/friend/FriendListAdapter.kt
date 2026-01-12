package com.example.habittracker.ui.social.friend

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.FriendRequest
import com.example.habittracker.data.model.User
import com.example.habittracker.databinding.ItemEmptyStateBinding
import com.example.habittracker.databinding.ItemFriendBinding
import com.example.habittracker.databinding.ItemFriendRequestBinding
import com.example.habittracker.databinding.ItemGlobalUserBinding
import com.example.habittracker.databinding.ItemSearchHeaderBinding
import com.example.habittracker.databinding.ItemSectionHeaderBinding
import com.example.habittracker.ui.social.profile.FriendListItem

/**
 * FriendListAdapter - Adapter for displaying friend list with multiple view types
 */
class FriendListAdapter(
    private val currentUserId: String,
    private val showUnfriendAction: Boolean,
    private val onSearchQueryChanged: (String) -> Unit,
    private val onAcceptRequest: (FriendRequest) -> Unit,
    private val onRejectRequest: (FriendRequest) -> Unit,
    private val onViewProfile: (User) -> Unit,
    private val onUnfriend: (User) -> Unit,
    private val onAddFriend: (User) -> Unit
) : ListAdapter<FriendListItem, RecyclerView.ViewHolder>(FriendListDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SEARCH_HEADER = 0
        private const val VIEW_TYPE_SECTION_HEADER = 1
        private const val VIEW_TYPE_REQUEST = 2
        private const val VIEW_TYPE_FRIEND = 3
        private const val VIEW_TYPE_GLOBAL_USER = 4
        private const val VIEW_TYPE_EMPTY = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FriendListItem.SearchHeader -> VIEW_TYPE_SEARCH_HEADER
            is FriendListItem.SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is FriendListItem.RequestItem -> VIEW_TYPE_REQUEST
            is FriendListItem.FriendItem -> VIEW_TYPE_FRIEND
            is FriendListItem.GlobalUserItem -> VIEW_TYPE_GLOBAL_USER
            is FriendListItem.EmptyState -> VIEW_TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEARCH_HEADER -> {
                val binding = ItemSearchHeaderBinding.inflate(inflater, parent, false)
                SearchHeaderViewHolder(binding, onSearchQueryChanged)
            }
            VIEW_TYPE_SECTION_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(inflater, parent, false)
                SectionHeaderViewHolder(binding)
            }
            VIEW_TYPE_REQUEST -> {
                val binding = ItemFriendRequestBinding.inflate(inflater, parent, false)
                RequestViewHolder(binding, onAcceptRequest, onRejectRequest)
            }
            VIEW_TYPE_FRIEND -> {
                val binding = ItemFriendBinding.inflate(inflater, parent, false)
                FriendViewHolder(binding, onViewProfile, onUnfriend, currentUserId, showUnfriendAction)
            }
            VIEW_TYPE_GLOBAL_USER -> {
                val binding = ItemGlobalUserBinding.inflate(inflater, parent, false)
                GlobalUserViewHolder(binding, onAddFriend)
            }
            VIEW_TYPE_EMPTY -> {
                val binding = ItemEmptyStateBinding.inflate(inflater, parent, false)
                EmptyStateViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is FriendListItem.SearchHeader -> {
                // Search header doesn't need binding
            }
            is FriendListItem.SectionHeader -> {
                (holder as SectionHeaderViewHolder).bind(item)
            }
            is FriendListItem.RequestItem -> {
                (holder as RequestViewHolder).bind(item.request)
            }
            is FriendListItem.FriendItem -> {
                (holder as FriendViewHolder).bind(item.friend)
            }
            is FriendListItem.GlobalUserItem -> {
                (holder as GlobalUserViewHolder).bind(item.user)
            }
            is FriendListItem.EmptyState -> {
                (holder as EmptyStateViewHolder).bind(item)
            }
        }
    }

    // Search Header ViewHolder
    class SearchHeaderViewHolder(
        private val binding: ItemSearchHeaderBinding,
        private val onSearchQueryChanged: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.etSearchFriends.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    onSearchQueryChanged(s?.toString() ?: "")
                }
                
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    // Section Header ViewHolder
    class SectionHeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: FriendListItem.SectionHeader) {
            binding.tvSectionTitle.text = header.title
            if (header.showBadge) {
                binding.tvCount.visibility = View.VISIBLE
                binding.tvCount.text = header.count.toString()
            } else {
                binding.tvCount.visibility = View.GONE
            }
        }
    }

    // Friend Request ViewHolder
    class RequestViewHolder(
        private val binding: ItemFriendRequestBinding,
        private val onAccept: (FriendRequest) -> Unit,
        private val onReject: (FriendRequest) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: FriendRequest) {
            binding.tvName.text = request.senderName
            // Use mutual friends or default text
             binding.tvMutualFriends.text = "Sent you a request"

            // Load avatar
            if (request.senderAvatarUrl.isEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            } else {
                Glide.with(itemView.context)
                    .load(request.senderAvatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAvatar)
            }

            // Button clicks
            binding.btnAccept.setOnClickListener {
                onAccept(request)
            }

            binding.btnReject.setOnClickListener {
                onReject(request)
            }
        }
    }

    // Friend ViewHolder
    class FriendViewHolder(
        private val binding: ItemFriendBinding,
        private val onViewProfile: (User) -> Unit,
        private val onUnfriend: (User) -> Unit,
        private val currentUserId: String,
        private val showUnfriendAction: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: User) {
            binding.tvName.text = friend.name
            binding.tvStreak.text = "${friend.points} Points"

            // Load avatar
            if (friend.avatarUrl.isNullOrEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            } else {
                Glide.with(itemView.context)
                    .load(friend.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAvatar)
            }

            // Unfriend Button Validation
            if (showUnfriendAction && friend.id != currentUserId) {
                 binding.btnUnfriend.visibility = View.VISIBLE
            } else {
                 binding.btnUnfriend.visibility = View.GONE
            }

            // View Profile Button Validation
            // Hide if it's the current user
            if (friend.id == currentUserId) {
                binding.btnViewProfile.visibility = View.GONE
            } else {
                binding.btnViewProfile.visibility = View.VISIBLE
            }

            // Button clicks
            binding.btnViewProfile.setOnClickListener {
                onViewProfile(friend)
            }

            binding.btnUnfriend.setOnClickListener {
                onUnfriend(friend)
            }
        }
    }
    
    // Global User ViewHolder
    class GlobalUserViewHolder(
        private val binding: ItemGlobalUserBinding,
        private val onAddFriend: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvName.text = user.name
            
            // Load avatar
            if (user.avatarUrl.isNullOrEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            } else {
                Glide.with(itemView.context)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAvatar)
            }

            // Button clicks
            binding.btnAddFriend.setOnClickListener {
                onAddFriend(user)
            }
        }
    }

    // Empty State ViewHolder
    class EmptyStateViewHolder(
        private val binding: ItemEmptyStateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emptyState: FriendListItem.EmptyState) {
            binding.tvEmptyMessage.text = emptyState.message
        }
    }
}

/**
 * DiffUtil callback for efficient list updates
 */
class FriendListDiffCallback : DiffUtil.ItemCallback<FriendListItem>() {
    override fun areItemsTheSame(oldItem: FriendListItem, newItem: FriendListItem): Boolean {
        return when {
            oldItem is FriendListItem.SearchHeader && newItem is FriendListItem.SearchHeader -> true
            oldItem is FriendListItem.SectionHeader && newItem is FriendListItem.SectionHeader -> 
                oldItem.title == newItem.title
            oldItem is FriendListItem.RequestItem && newItem is FriendListItem.RequestItem ->
                oldItem.request.id == newItem.request.id
            oldItem is FriendListItem.FriendItem && newItem is FriendListItem.FriendItem ->
                oldItem.friend.id == newItem.friend.id
            oldItem is FriendListItem.GlobalUserItem && newItem is FriendListItem.GlobalUserItem ->
                oldItem.user.id == newItem.user.id
            oldItem is FriendListItem.EmptyState && newItem is FriendListItem.EmptyState -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: FriendListItem, newItem: FriendListItem): Boolean {
        return oldItem == newItem
    }
}
