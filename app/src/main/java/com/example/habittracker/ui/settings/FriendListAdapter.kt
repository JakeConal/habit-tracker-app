package com.example.habittracker.ui.settings

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
import com.example.habittracker.databinding.ItemEmptyStateBinding
import com.example.habittracker.databinding.ItemFriendBinding
import com.example.habittracker.databinding.ItemFriendRequestBinding
import com.example.habittracker.databinding.ItemSearchHeaderBinding
import com.example.habittracker.databinding.ItemSectionHeaderBinding

/**
 * FriendListAdapter - Adapter for displaying friend list with multiple view types
 */
class FriendListAdapter(
    private val onSearchQueryChanged: (String) -> Unit,
    private val onAcceptRequest: (FriendRequest) -> Unit,
    private val onRejectRequest: (FriendRequest) -> Unit,
    private val onViewProfile: (Friend) -> Unit,
    private val onUnfriend: (Friend) -> Unit
) : ListAdapter<FriendListItem, RecyclerView.ViewHolder>(FriendListDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SEARCH_HEADER = 0
        private const val VIEW_TYPE_SECTION_HEADER = 1
        private const val VIEW_TYPE_REQUEST = 2
        private const val VIEW_TYPE_FRIEND = 3
        private const val VIEW_TYPE_EMPTY = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FriendListItem.SearchHeader -> VIEW_TYPE_SEARCH_HEADER
            is FriendListItem.SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is FriendListItem.RequestItem -> VIEW_TYPE_REQUEST
            is FriendListItem.FriendItem -> VIEW_TYPE_FRIEND
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
                FriendViewHolder(binding, onViewProfile, onUnfriend)
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
            binding.tvName.text = request.name
            binding.tvMutualFriends.text = 
                itemView.context.getString(R.string.mutual_friends_format, request.mutualFriendsCount)

            // Load avatar
            if (request.avatarUrl.isEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            } else {
                Glide.with(itemView.context)
                    .load(request.avatarUrl)
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
        private val onViewProfile: (Friend) -> Unit,
        private val onUnfriend: (Friend) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.tvName.text = friend.name
            binding.tvStreak.text = 
                itemView.context.getString(R.string.day_streak_format, friend.currentStreak)

            // Load avatar
            if (friend.avatarUrl.isEmpty()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            } else {
                Glide.with(itemView.context)
                    .load(friend.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAvatar)
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
            oldItem is FriendListItem.EmptyState && newItem is FriendListItem.EmptyState -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: FriendListItem, newItem: FriendListItem): Boolean {
        return oldItem == newItem
    }
}
