package com.example.habittracker.ui.social.friend

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
import com.example.habittracker.ui.feed.Post
import com.example.habittracker.ui.feed.PostAdapter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get friendId from arguments
        val friendId = arguments?.getString("friendId") ?: ""
        
        // Load friend profile data
        viewModel.loadFriendProfile(friendId)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onLikeClick = { _ ->
                Toast.makeText(
                    requireContext(),
                    "Like feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCommentClick = { _ ->
                Toast.makeText(
                    requireContext(),
                    "Comment feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onMoreOptionsClick = { post: Post, anchorView: View ->
                val popupMenu = android.widget.PopupMenu(requireContext(), anchorView)
                popupMenu.menu.add("Share")

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Share" -> {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this habit update!")
                                putExtra(android.content.Intent.EXTRA_TEXT, "${post.content}\n\nShared from Habit Tracker App")
                            }
                            startActivity(android.content.Intent.createChooser(shareIntent, "Share post via"))
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
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Settings button
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

    private fun updateTabUI(tab: FriendProfileViewModel.ProfileTab) {
        when (tab) {
            FriendProfileViewModel.ProfileTab.MY_POST -> {
                binding.btnPostsTab.setBackgroundResource(R.drawable.bg_friend_tab_selected)
                binding.btnPostsTab.setTextColor(resources.getColor(R.color.white, null))
                binding.btnFriendsTab.setBackgroundResource(R.drawable.bg_friend_tab_unselected)
                binding.btnFriendsTab.setTextColor(resources.getColor(R.color.friend_profile_text_tertiary, null))
            }
            FriendProfileViewModel.ProfileTab.MY_FRIENDS -> {
                binding.btnPostsTab.setBackgroundResource(R.drawable.bg_friend_tab_unselected)
                binding.btnPostsTab.setTextColor(resources.getColor(R.color.friend_profile_text_tertiary, null))
                binding.btnFriendsTab.setBackgroundResource(R.drawable.bg_friend_tab_selected)
                binding.btnFriendsTab.setTextColor(resources.getColor(R.color.white, null))
            }
        }
    }

    private fun updateContentVisibility(showEmpty: Boolean) {
        if (showEmpty) {
            binding.rvPosts.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvPosts.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
