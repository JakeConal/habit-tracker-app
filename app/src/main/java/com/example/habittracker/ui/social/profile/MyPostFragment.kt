package com.example.habittracker.ui.social.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.databinding.FragmentMyPostBinding
import com.example.habittracker.data.model.Post
import com.example.habittracker.ui.feed.CommentsActivity
import com.example.habittracker.ui.feed.CreatePostActivity
import com.example.habittracker.ui.feed.PostAdapter
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.launch

class MyPostFragment : Fragment() {

    private var _binding: FragmentMyPostBinding? = null
    private val binding get() = _binding!!

    // Use parent fragment (ProfileFragment) viewmodel to share data
    private lateinit var viewModel: ProfileViewModel
    private lateinit var postAdapter: PostAdapter

    private val commentsLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { data ->
                val postId = data.getStringExtra(CommentsActivity.RESULT_POST_ID)
                val newCommentCount = data.getIntExtra(CommentsActivity.RESULT_COMMENT_COUNT, 0)
                val newLikeCount = data.getIntExtra(CommentsActivity.RESULT_LIKE_COUNT, 0)
                val isLiked = data.getBooleanExtra(CommentsActivity.RESULT_IS_LIKED, false)

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
        _binding = FragmentMyPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get shared ViewModel from parent fragment
        viewModel = ViewModelProvider(requireParentFragment())[ProfileViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val currentUserId = UserPreferences.getUserId(requireContext())
        postAdapter = PostAdapter(
            currentUserId = currentUserId,
            onLikeClick = { post ->
                val senderName = UserPreferences.getUserName(requireContext())
                val senderAvatar = UserPreferences.getUserAvatar(requireContext())
                viewModel.toggleLike(post.id, senderName, senderAvatar)
            },
            onCommentClick = { post ->
                val isLiked = post.likedBy.contains(currentUserId)
                val intent = Intent(requireContext(), CommentsActivity::class.java).apply {
                    putExtra(CommentsActivity.EXTRA_POST_ID, post.id)
                    putExtra(CommentsActivity.EXTRA_POST_USER_ID, post.userId)
                    putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
                    putExtra(CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatarUrl)
                    putExtra(CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
                    putExtra(CommentsActivity.EXTRA_CONTENT, post.content)
                    putExtra(CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
                    putExtra(CommentsActivity.EXTRA_LIKES_COUNT, post.likeCount)
                    putExtra(CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentCount)
                    putExtra(CommentsActivity.EXTRA_IS_LIKED, isLiked)
                    putParcelableArrayListExtra(CommentsActivity.EXTRA_COMMENTS, java.util.ArrayList<com.example.habittracker.data.model.Comment>())

                    // Pass shared post data
                    if (!post.originalPostId.isNullOrEmpty()) {
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_POST_ID, post.originalPostId)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_USER_ID, post.originalUserId)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_NAME, post.originalAuthorName)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_AVATAR, post.originalAuthorAvatarUrl)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_CONTENT, post.originalContent)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_IMAGE_URL, post.originalImageUrl)
                    }
                }
                commentsLauncher.launch(intent)
            },
            onShareClick = { post ->
                val intent = Intent(requireContext(), CreatePostActivity::class.java).apply {
                    putExtra("EXTRA_SHARED_POST", post)
                }
                startActivity(intent)
            },
            onAuthorClick = { _ ->
                // Since this is the user's own profile, clicking the author doesn't need to do much.
                // But for consistency, let's just make sure it's handled.
                // User is already on their profile.
            },
            onMoreOptionsClick = { post: Post, anchorView: View ->
                val popupMenu = PopupMenu(requireContext(), anchorView)
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
                            val intent = Intent(requireContext(), CreatePostActivity::class.java).apply {
                                putExtra("EXTRA_SHARED_POST", post)
                            }
                            startActivity(intent)
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                if (posts.isEmpty()) {
                    binding.rvPosts.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.rvPosts.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    postAdapter.submitList(posts)
                }
            }
        }
        
        // Observe loading
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        // Observe error from parent ViewModel
        lifecycleScope.launch {
            viewModel.error.collect { errorMsg ->
                if (!errorMsg.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MyPostFragment()
    }
}
