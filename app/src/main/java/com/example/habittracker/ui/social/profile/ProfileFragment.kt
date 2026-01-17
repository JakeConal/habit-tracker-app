package com.example.habittracker.ui.social.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

/**
 * ProfileFragment - User profile screen with posts and friends tabs
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

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
        setupViewPager()
        observeViewModel()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh posts when returning to the fragment
        viewModel.refreshPosts()
    }

    private fun setupViews() {
        // Set user info
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvUserName.text = name
                    }
                }
                launch {
                    viewModel.userEmail.collect { email ->
                        binding.tvUserEmail.text = email
                    }
                }
                launch {
                    viewModel.userAvatarUrl.collect { avatarUrl ->
                        loadAvatar(avatarUrl)
                    }
                }
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

    private fun setupViewPager() {
        val adapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Disable swipe to change tab as we use custom tab buttons
        binding.viewPager.isUserInputEnabled = false 
    }

    private fun observeViewModel() {
        // Observe selected tab
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.selectedTab.collect { tab ->
                    updateTabUI(tab)
                    val currentItem = if (tab == ProfileViewModel.ProfileTab.MY_POST) 0 else 1
                    if (binding.viewPager.currentItem != currentItem) {
                        binding.viewPager.setCurrentItem(currentItem, false)
                    }
                }
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
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
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
