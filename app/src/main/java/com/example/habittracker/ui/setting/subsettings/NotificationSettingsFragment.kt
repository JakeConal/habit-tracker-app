package com.example.habittracker.ui.setting.subsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.databinding.FragmentNotificationSettingsBinding
import com.example.habittracker.utils.UserPreferences
import kotlinx.coroutines.launch

class NotificationSettingsFragment : Fragment() {
    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        // Load current state
        val isEnabled = UserPreferences.areNotificationsEnabled(requireContext())
        binding.switchNotifications.isChecked = isEnabled
        
        // Handle toggle change
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setNotificationsEnabled(requireContext(), isChecked)

            // Update Firestore
            val userId = UserPreferences.getUserId(requireContext())
            lifecycleScope.launch {
                FirestoreUserRepository.getInstance().updateNotificationsEnabled(userId, isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
