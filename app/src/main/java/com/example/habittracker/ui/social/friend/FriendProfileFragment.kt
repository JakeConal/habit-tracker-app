package com.example.habittracker.ui.social.friend

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentFriendProfileBinding
import com.example.habittracker.data.model.Post
import com.example.habittracker.ui.feed.PostAdapter
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.launch

/**
 * FriendProfileFragment - Displays a friend's profile with their posts
 */
class FriendProfileFragment : Fragment() {

    private var _binding: FragmentFriendProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendProfileViewModel by viewModels()
    
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var friendListAdapter: FriendListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get friendId from arguments
        val friendId = arguments?.getString("friendId") ?: ""
        
        setupRecyclerViews(friendId)
        setupClickListeners()
        observeViewModel()
        
        // Load friend profile data
        viewModel.loadFriendProfile(friendId)
    }

    private fun setupRecyclerViews(friendId: String) {
        // Posts Adapter
        val currentUserId = UserPreferences.getUserId(requireContext())
        postAdapter = PostAdapter(
            currentUserId = currentUserId,
            onLikeClick = { _ ->
                Toast.makeText(requireContext(), "Like feature coming soon!", Toast.LENGTH_SHORT).show()
            },
            onCommentClick = { _ ->
                Toast.makeText(requireContext(), "Comment feature coming soon!", Toast.LENGTH_SHORT).show()
            },
            onAuthorClick = { userId ->
                if (userId == currentUserId) {
                    findNavController().navigate(R.id.nav_profile)
                } else if (userId != friendId) {
                    val bundle = Bundle().apply {
                        putString("friendId", userId)
                    }
                    findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
                }
            },
            onShareClick = { post ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check out this habit update!")
                    putExtra(Intent.EXTRA_TEXT, "${post.content}\n\nShared from Habit Tracker App")
                }
                startActivity(Intent.createChooser(shareIntent, "Share post via"))
            },
            onMoreOptionsClick = { post: Post, anchorView: View ->
                // Share logic...
            }
        )

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            setHasFixedSize(false)
        }

        // Friends Adapter
        friendListAdapter = FriendListAdapter(
            currentUserId = currentUserId,
            showUnfriendAction = false,
            onSearchQueryChanged = {}, // No search here
            onAcceptRequest = {},
            onRejectRequest = {},
            onViewProfile = { friend ->
                // Navigate to this friend's profile from the list
                 val bundle = Bundle().apply {
                    putString("friendId", friend.id)
                }
                findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
            },
            onUnfriend = { _ -> }, // Cannot unfriend unrelated people from here
            onAddFriend = { _ -> }
        )

        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendListAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Settings button -> TODO: maybe Unfriend here?
        binding.btnSettings.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.friend_profile_feature_coming_soon),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Tab buttons
        binding.btnPostsTab.setOnClickListener {
            viewModel.selectTab(FriendProfileViewModel.ProfileTab.MY_POST)
        }

        binding.btnFriendsTab.setOnClickListener {
            viewModel.selectTab(FriendProfileViewModel.ProfileTab.MY_FRIENDS)
        }

        // Add Friend button
        binding.btnAddFriend.setOnClickListener {
            viewModel.sendFriendRequest()
        }
    }

    private fun observeViewModel() {
        // Observe friend profile data
        lifecycleScope.launch {
            viewModel.friendProfile.collect { profile ->
                profile?.let {
                    binding.tvHeaderName.text = it.name
                    binding.tvUserName.text = it.name
                    binding.tvUserEmail.text = it.email
                    loadAvatar(it.avatarUrl)
                }
            }
        }

        // Observe posts
        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                postAdapter.submitList(posts)
            }
        }

        // Observe friends
        lifecycleScope.launch {
            viewModel.friendListItems.collect { items ->
                friendListAdapter.submitList(items)
            }
        }

        // Observe selected tab
        lifecycleScope.launch {
            viewModel.selectedTab.collect { tab ->
                updateTabUI(tab)
            }
        }

        // Observe empty state
        lifecycleScope.launch {
            viewModel.showEmptyState.collect { showEmpty ->
                updateContentVisibility(showEmpty)
            }
        }

        // Observe empty state message
        lifecycleScope.launch {
            viewModel.emptyStateMessage.collect { message ->
                binding.tvEmptyState.text = message
            }
        }

        // Observe friendship status
        lifecycleScope.launch {
            viewModel.friendshipStatus.collect { status ->
                updateFriendshipUI(status)
            }
        }
    }

    private fun loadAvatar(avatarUrl: String) {
        if (avatarUrl.isEmpty()) {
            binding.ivProfileAvatar.setImageResource(R.drawable.ic_person)
        } else {
            Glide.with(this)
                .load(avatarUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivProfileAvatar)
        }
    }

    private fun updateFriendshipUI(status: FriendProfileViewModel.FriendshipStatus) {
        when (status) {
            FriendProfileViewModel.FriendshipStatus.NOT_FRIEND -> {
                binding.btnAddFriend.visibility = View.VISIBLE
                binding.btnAddFriend.isEnabled = true
                binding.btnAddFriend.alpha = 1.0f
            }
            FriendProfileViewModel.FriendshipStatus.PENDING -> {
                binding.btnAddFriend.visibility = View.VISIBLE
                binding.btnAddFriend.isEnabled = false
                binding.btnAddFriend.alpha = 1.0f
            }
            FriendProfileViewModel.FriendshipStatus.FRIEND -> {
                binding.btnAddFriend.visibility = View.GONE
            }
            FriendProfileViewModel.FriendshipStatus.SELF -> {
                binding.btnAddFriend.visibility = View.GONE
            }
        }
    }

    private fun updateTabUI(tab: FriendProfileViewModel.ProfileTab) {
        when (tab) {
            FriendProfileViewModel.ProfileTab.MY_POST -> {
                binding.btnPostsTab.setBackgroundResource(R.drawable.bg_friend_tab_selected)
                binding.btnPostsTab.setTextColor(resources.getColor(R.color.white, null))
                binding.btnFriendsTab.setBackgroundResource(R.drawable.bg_friend_tab_unselected)
                binding.btnFriendsTab.setTextColor(resources.getColor(R.color.friend_profile_text_tertiary, null))
                
                binding.rvPosts.visibility = if (viewModel.showEmptyState.value) View.GONE else View.VISIBLE
                binding.rvFriends.visibility = View.GONE
            }
            FriendProfileViewModel.ProfileTab.MY_FRIENDS -> {
                binding.btnPostsTab.setBackgroundResource(R.drawable.bg_friend_tab_unselected)
                binding.btnPostsTab.setTextColor(resources.getColor(R.color.friend_profile_text_tertiary, null))
                binding.btnFriendsTab.setBackgroundResource(R.drawable.bg_friend_tab_selected)
                binding.btnFriendsTab.setTextColor(resources.getColor(R.color.white, null))
                
                binding.rvPosts.visibility = View.GONE
                binding.rvFriends.visibility = if (viewModel.showEmptyState.value) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateContentVisibility(showEmpty: Boolean) {
        val tab = viewModel.selectedTab.value
        binding.tvEmptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
        
        if (showEmpty) {
            binding.rvPosts.visibility = View.GONE
            binding.rvFriends.visibility = View.GONE
        } else {
            when (tab) {
                FriendProfileViewModel.ProfileTab.MY_POST -> {
                    binding.rvPosts.visibility = View.VISIBLE
                    binding.rvFriends.visibility = View.GONE
                }
                FriendProfileViewModel.ProfileTab.MY_FRIENDS -> {
                    binding.rvPosts.visibility = View.GONE
                    binding.rvFriends.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
