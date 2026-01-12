package com.example.habittracker.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

/**
 * SettingsFragment - App settings and preferences
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var settingsMenuAdapter: SettingsMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnSettings.setOnClickListener {
            // Settings button in header - could open additional options
        }
    }
    
    private fun setupRecyclerView() {
        settingsMenuAdapter = SettingsMenuAdapter { menuItem ->
            handleMenuItemClick(menuItem)
        }
        
        binding.rvSettingsMenu.apply {
            adapter = settingsMenuAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.settingsMenuItems.collect { items ->
                settingsMenuAdapter.submitList(items)
            }
        }
    }
    
    private fun handleMenuItemClick(menuItem: SettingMenuItem) {
        when (menuItem.id) {
            1 -> navigateToEditProfile()
            2 -> navigateToResetPassword()
            3 -> navigateToNotificationSettings()
            4 -> navigateToTerms()
            5 -> navigateToReviewChallenge()
            6 -> handleLogout()
        }
    }
    
    private fun navigateToEditProfile() {
        findNavController().navigate(R.id.action_settingsFragment_to_editProfileFragment)
    }
    
    private fun navigateToResetPassword() {
        // TODO: Navigate to reset password screen
        // findNavController().navigate(R.id.action_settingsFragment_to_resetPasswordFragment)
    }
    
    private fun navigateToNotificationSettings() {
        // TODO: Navigate to notification settings screen
        // findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
    }
    
    private fun navigateToTerms() {
        // TODO: Navigate to terms screen
        // findNavController().navigate(R.id.action_settingsFragment_to_termsFragment)
    }
    
    private fun navigateToReviewChallenge() {
        // TODO: Navigate to review challenge screen
        // findNavController().navigate(R.id.action_settingsFragment_to_reviewChallengeFragment)
    }
    
    private fun handleLogout() {
        // TODO: Implement logout functionality
        // Show confirmation dialog and handle logout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

