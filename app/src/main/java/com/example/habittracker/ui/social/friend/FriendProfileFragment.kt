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
import com.example.habittracker.util.UserPreferences
import kotlinx.coroutines.launch

/**
 * FriendProfileFragment - Displays a friend's profile with their posts
 */
class FriendProfileFragment : Fragment() {

    private var _binding: FragmentFriendProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendProfileViewModel by viewModels()
    
    private lateinit var postAdapter: PostAdapter

    private val commentsLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { data ->
                val postId = data.getStringExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_POST_ID)
                val newCommentCount = data.getIntExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_COMMENT_COUNT, 0)
                val newLikeCount = data.getIntExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_LIKE_COUNT, 0)
                val isLiked = data.getBooleanExtra(com.example.habittracker.ui.feed.CommentsActivity.RESULT_IS_LIKED, false)

                // Update the post in the list via ViewModel
                viewModel.updatePost(postId, newCommentCount, newLikeCount, isLiked)
            }
        }
    }

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

    private fun openCommentsActivity(post: Post) {
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

            // Pass shared post data
            if (!post.originalPostId.isNullOrEmpty()) {
                putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_ORIGINAL_POST_ID, post.originalPostId)
                putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_ORIGINAL_USER_ID, post.originalUserId)
                putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_ORIGINAL_AUTHOR_NAME, post.originalAuthorName)
                putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_ORIGINAL_AUTHOR_AVATAR, post.originalAuthorAvatarUrl)
                putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_ORIGINAL_CONTENT, post.originalContent)
                putExtra(com.example.habittracker.ui.feed.CommentsActivity.EXTRA_ORIGINAL_IMAGE_URL, post.originalImageUrl)
            }
        }
        commentsLauncher.launch(intent)
    }

    private fun setupRecyclerViews(friendId: String) {
        // Posts Adapter
        val currentUserId = UserPreferences.getUserId(requireContext())
        postAdapter = PostAdapter(
            currentUserId,
            { post -> viewModel.toggleLike(post) }, // onLikeClick
            { post -> openCommentsActivity(post) }, // onCommentClick
            { post -> // onShareClick
                lifecycleScope.launch {
                    val senderName = UserPreferences.getUserName(requireContext())
                    val senderAvatar = UserPreferences.getUserAvatar(requireContext())
                    com.example.habittracker.data.repository.PostRepository.getInstance()
                        .sharePost(post.id, senderName, senderAvatar)
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${post.content}\n\nShared from Habit Tracker App")
                }
                startActivity(Intent.createChooser(shareIntent, "Share post via"))
            },
            { userId -> // onAuthorClick
                if (userId == currentUserId) {
                    findNavController().navigate(R.id.nav_profile)
                } else if (userId != friendId) {
                    val bundle = Bundle().apply { putString("friendId", userId) }
                    findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
                }
            },
            { post -> voteForChallenge(post) }, // onVoteClick
            { _, _ -> }, // onMoreOptionsClick
            emptyList() // votedChallengeIds
        )

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            setHasFixedSize(false)
        }

        // Friends Adapter
        friendListAdapter = FriendListAdapter(
            currentUserId,
            false, // showUnfriendAction
            {}, // onSearchQueryChanged
            {}, // onAcceptRequest
            {}, // onRejectRequest
            { friend -> // onViewProfile
                 val bundle = Bundle().apply { putString("friendId", friend.id) }
                 findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
            },
            { _ -> }, // onUnfriend
            { _ -> } // onAddFriend
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

        // Observe voted challenges
        lifecycleScope.launch {
            viewModel.votedChallengeIds.collect { votedIds ->
                postAdapter.updateVotedChallengeIds(votedIds)
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

    private fun voteForChallenge(post: Post) {
        viewModel.voteForChallenge(post)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
