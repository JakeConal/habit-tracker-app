package com.example.habittracker.ui.social.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentProfileBinding
import com.example.habittracker.data.model.Post
import com.example.habittracker.ui.feed.PostAdapter
import com.example.habittracker.ui.social.friend.FriendListAdapter
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.launch

/**
 * ProfileFragment - User profile screen with posts and friends tabs
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter
    private lateinit var friendListAdapter: FriendListAdapter

    private val commentsLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { data ->
                val postId = data.getStringExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_POST_ID)
                val newCommentCount = data.getIntExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_COMMENT_COUNT, 0)
                val newLikeCount = data.getIntExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_LIKE_COUNT, 0)
                val isLiked = data.getBooleanExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_IS_LIKED, false)

                if (postId != null) {
                    viewModel.updatePost(postId, newCommentCount, newLikeCount, isLiked)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupViews() {
        // Set user info
        lifecycleScope.launch {
            viewModel.userName.collect { name ->
                binding.tvUserName.text = name
            }
        }

        lifecycleScope.launch {
            viewModel.userEmail.collect { email ->
                binding.tvUserEmail.text = email
            }
        }

        lifecycleScope.launch {
            viewModel.userAvatarUrl.collect { avatarUrl ->
                loadAvatar(avatarUrl)
            }
        }
    }

    private fun loadAvatar(avatarUrl: String) {
        if (avatarUrl.isEmpty()) {
            // Use local placeholder
            binding.ivProfileAvatar.setImageResource(R.drawable.ic_person)
        } else {
            // Load from URL using Glide
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivProfileAvatar)
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = UserPreferences.getUserId(requireContext())
        // Post adapter
        postAdapter = PostAdapter(
            currentUserId = currentUserId,
            onLikeClick = { post ->
                viewModel.toggleLike(post.id)
            },
            onCommentClick = { post ->
                val currentUserId = UserPreferences.getUserId(requireContext())
                val isLiked = post.likedBy.contains(currentUserId)

                val intent = Intent(requireContext(), com.example.habittracker.ui.feed.CommentsActivity::class.java).apply {
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_POST_ID, post.id)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_POST_USER_ID, post.userId)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatarUrl)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_CONTENT, post.content)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_LIKES_COUNT, post.likeCount)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentCount)
                    putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_IS_LIKED, isLiked)
                    putParcelableArrayListExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_COMMENTS, java.util.ArrayList<com.example.habittracker.data.model.Comment>())
                }
                commentsLauncher.launch(intent)
            },
            onMoreOptionsClick = { post: Post, anchorView: View ->
                // Basic implementation for Profile - can be customized
                val popupMenu = PopupMenu(requireContext(), anchorView)
                // Profile usually shows own posts so default is Delete
                popupMenu.menu.add("Delete")
                popupMenu.menu.add("Share")

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Delete" -> {
                            // TODO: Call delete in ViewModel
                            Toast.makeText(requireContext(), "Delete clicked", Toast.LENGTH_SHORT).show()
                            true
                        }
                        "Share" -> {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Check out this habit update!")
                                putExtra(Intent.EXTRA_TEXT, "${post.content}\n\nShared from Habit Tracker App")
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share post via"))
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        )

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            setHasFixedSize(false)
        }

        // Friend list adapter
        friendListAdapter = FriendListAdapter(
            onSearchQueryChanged = { query ->
                viewModel.updateSearchQuery(query)
            },
            onAcceptRequest = { request ->
                Toast.makeText(
                    requireContext(),
                    "Accept request from ${request.name} - Feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onRejectRequest = { request ->
                Toast.makeText(
                    requireContext(),
                    "Reject request from ${request.name} - Feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onViewProfile = { friend ->
                // Navigate to friend profile
                val bundle = Bundle().apply {
                    putString("friendId", friend.userId)
                }
                findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
            },
            onUnfriend = { friend ->
                Toast.makeText(
                    requireContext(),
                    "Unfriend ${friend.name} - Feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvFriendsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendListAdapter
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        // Observe posts
        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                postAdapter.submitList(posts)
            }
        }

        // Observe filtered friend list items
        lifecycleScope.launch {
            viewModel.filteredFriendListItems.collect { items ->
                friendListAdapter.submitList(items)
            }
        }

        // Observe selected tab
        lifecycleScope.launch {
            viewModel.selectedTab.collect { tab ->
                updateTabUI(tab)
                updateContentVisibility(tab)
            }
        }
    }

    private fun updateTabUI(tab: ProfileViewModel.ProfileTab) {
        when (tab) {
            ProfileViewModel.ProfileTab.MY_POST -> {
                binding.btnMyPost.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.btnMyPost.setTextColor(resources.getColor(R.color.white, null))
                binding.btnMyFriends.setBackgroundResource(android.R.color.transparent)
                binding.btnMyFriends.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            ProfileViewModel.ProfileTab.MY_FRIENDS -> {
                binding.btnMyPost.setBackgroundResource(android.R.color.transparent)
                binding.btnMyPost.setTextColor(resources.getColor(R.color.text_secondary, null))
                binding.btnMyFriends.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.btnMyFriends.setTextColor(resources.getColor(R.color.white, null))
            }
        }
    }

    private fun updateContentVisibility(tab: ProfileViewModel.ProfileTab) {
        when (tab) {
            ProfileViewModel.ProfileTab.MY_POST -> {
                binding.rvPosts.visibility = View.VISIBLE
                binding.rvFriendsList.visibility = View.GONE
                binding.emptyFriendsState.visibility = View.GONE
            }
            ProfileViewModel.ProfileTab.MY_FRIENDS -> {
                binding.rvPosts.visibility = View.GONE
                binding.rvFriendsList.visibility = View.VISIBLE
                binding.emptyFriendsState.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Settings button
        binding.btnSettings.setOnClickListener {
            // Navigate to SettingsFragment
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        // Edit profile button
        binding.btnEditProfile.setOnClickListener {
            // TODO: Navigate to edit profile screen or show dialog
        }

        // Tab buttons
        binding.btnMyPost.setOnClickListener {
            viewModel.selectTab(ProfileViewModel.ProfileTab.MY_POST)
        }

        binding.btnMyFriends.setOnClickListener {
            viewModel.selectTab(ProfileViewModel.ProfileTab.MY_FRIENDS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
