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
            7 -> handleDeleteAccount()
        }
    }
    
    private fun navigateToEditProfile() {
        findNavController().navigate(R.id.action_settingsFragment_to_editProfileFragment)
    }
    
    private fun navigateToResetPassword() {
        findNavController().navigate(R.id.action_settingsFragment_to_resetPasswordFragment)
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
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_message)
            .setPositiveButton(R.string.logout_confirm_button) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        com.example.habittracker.data.repository.AuthRepository.getInstance().signOut()
        
        // Clear local preferences
        com.example.habittracker.utils.UserPreferences.clearUserData(requireContext())
        
        // Navigate to login and clear backstack
        val navOptions = androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph_main, true)
            .build()
        findNavController().navigate(R.id.nav_login, null, navOptions)
    }

    private fun handleDeleteAccount() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_account_confirm_title)
            .setMessage(R.string.delete_account_confirm_message)
            .setPositiveButton(R.string.delete_account_button) { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performDeleteAccount() {
        lifecycleScope.launch {
            // Show loading or disable UI if needed
            val result = com.example.habittracker.data.service.AccountDeletionService.getInstance()
                .deleteCurrentUserAccount()
            
            if (result.isSuccess) {
                android.widget.Toast.makeText(requireContext(), R.string.delete_account_success, android.widget.Toast.LENGTH_SHORT).show()
                
                // Clear local preferences
                com.example.habittracker.utils.UserPreferences.clearUserData(requireContext())
                
                // Navigate to login and clear backstack
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph_main, true)
                    .build()
                findNavController().navigate(R.id.nav_login, null, navOptions)
            } else {
                android.widget.Toast.makeText(requireContext(), R.string.delete_account_error, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

