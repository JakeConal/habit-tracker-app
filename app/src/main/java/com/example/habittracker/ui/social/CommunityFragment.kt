package com.example.habittracker.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.databinding.FragmentCommunityBinding
import com.example.habittracker.databinding.LayoutNotificationDropdownBinding
import com.example.habittracker.ui.feed.CommentsActivity
import com.example.habittracker.ui.notification.NotificationDropdownAdapter
import com.example.habittracker.ui.notification.NotificationViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/**
 * CommunityFragment - Màn hình Community chứa TabLayout và ViewPager2
 * Hiển thị 3 tabs: Feed, Challenges, Leaderboard
 */
class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private var notificationPopupWindow: PopupWindow? = null
    private lateinit var notificationAdapter: NotificationDropdownAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupNotificationAdapter()
        setupHeader()
        observeData()
    }

    private fun setupNotificationAdapter() {
        notificationAdapter = NotificationDropdownAdapter { notification ->
            notificationViewModel.markNotificationAsRead(notification.id)
            notificationPopupWindow?.dismiss()
            if (notification.postId.isNotEmpty()) {
                navigateToPost(notification.postId)
            }
        }
    }

    private fun navigateToPost(postId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val post = PostRepository.getInstance().getPostById(postId)
            if (post != null) {
                val intent = android.content.Intent(requireContext(), CommentsActivity::class.java).apply {
                    putExtra(CommentsActivity.EXTRA_POST_ID, post.id)
                    putExtra(CommentsActivity.EXTRA_POST_USER_ID, post.userId)
                    putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
                    putExtra(CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatarUrl)
                    putExtra(CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
                    putExtra(CommentsActivity.EXTRA_CONTENT, post.content)
                    putExtra(CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
                    putExtra(CommentsActivity.EXTRA_LIKES_COUNT, post.likeCount)
                    putExtra(CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentCount)
                    putExtra(CommentsActivity.EXTRA_IS_LIKED, post.likedBy.contains(com.example.habittracker.data.repository.AuthRepository.getInstance().getCurrentUser()?.uid))

                    // Add shared post data for full view
                    if (!post.originalPostId.isNullOrEmpty()) {
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_POST_ID, post.originalPostId)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_USER_ID, post.originalUserId)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_NAME, post.originalAuthorName)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_AVATAR, post.originalAuthorAvatarUrl)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_CONTENT, post.originalContent)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_IMAGE_URL, post.originalImageUrl)
                    }
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            notificationViewModel.notifications.collect { notifications ->
                notificationPopupWindow?.let { popup ->
                    if (popup.isShowing) {
                        notificationAdapter.submitList(notifications)
                    }
                }
            }
        }
    }

    private fun showNotificationDropdown(anchorView: View) {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val dropdownBinding = LayoutNotificationDropdownBinding.inflate(inflater)

        dropdownBinding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }

        updateNotificationList(dropdownBinding)

        dropdownBinding.btnSeeMore.setOnClickListener {
            notificationViewModel.setShowAllNotifications(true)
            updateNotificationList(dropdownBinding)
        }

        notificationPopupWindow = PopupWindow(
            dropdownBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 10f
            setBackgroundDrawable(null)
            showAsDropDown(anchorView, 0, 10, android.view.Gravity.END)
        }
    }

    private fun updateNotificationList(dropdownBinding: LayoutNotificationDropdownBinding) {
        val notifications = notificationViewModel.notifications.value
        val showAll = notificationViewModel.showAllNotifications.value

        if (notifications.size > 5 && !showAll) {
            notificationAdapter.submitList(notifications.take(5))
            dropdownBinding.btnSeeMore.visibility = View.VISIBLE
        } else {
            notificationAdapter.submitList(notifications)
            dropdownBinding.btnSeeMore.visibility = View.GONE
        }
    }

    /**
     * Setup ViewPager2 với TabLayout
     * Sử dụng childFragmentManager cho nested fragments
     */
    private fun setupViewPager() {
        // Sử dụng childFragmentManager thay vì activity's fragmentManager
        binding.viewPager.adapter = CommunityPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Feed"
                1 -> "Challenges"
                2 -> "Leaderboard"
                else -> null
            }
        }.attach()
    }

    /**
     * Setup header buttons
     */
    private fun setupHeader() {
        binding.btnBack.setOnClickListener {
            // Navigate back
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNotification.setOnClickListener {
            showNotificationDropdown(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CommunityFragment()
    }
}
