package com.example.habittracker.ui.setting.subsettings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.databinding.FragmentNotificationSettingsBinding
import com.example.habittracker.util.ReminderScheduler
import com.example.habittracker.util.UserPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * NotificationSettingsFragment - Manages app notifications and daily reminder settings
 */
class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    // Permission request launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable reminder
            enableReminder()
        } else {
            // Permission denied
            binding.switchReminder.isChecked = false
            showPermissionDeniedDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupAppNotificationsSwitch()
        setupReminderSwitch()
        setupReminderTimeClick()
        loadCurrentSettings()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { 
            findNavController().navigateUp() 
        }
    }

    /**
     * Setup the main app notifications switch
     */
    private fun setupAppNotificationsSwitch() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            UserPreferences.setNotificationsEnabled(requireContext(), isChecked)

            // Update Firestore
            val userId = UserPreferences.getUserId(requireContext())
            lifecycleScope.launch {
                FirestoreUserRepository.getInstance().updateNotificationsEnabled(userId, isChecked)
            }

            // If app notifications are disabled, also disable reminder
            if (!isChecked && binding.switchReminder.isChecked) {
                binding.switchReminder.isChecked = false
            }

            // Update reminder switch state
            binding.switchReminder.isEnabled = isChecked
        }
    }

    /**
     * Setup the daily reminder switch with permission handling
     */
    private fun setupReminderSwitch() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check notification permission before enabling
                if (checkNotificationPermission()) {
                    enableReminder()
                } else {
                    // Request permission
                    requestNotificationPermission()
                }
            } else {
                disableReminder()
            }
        }
    }

    /**
     * Setup click listener for reminder time row
     */
    private fun setupReminderTimeClick() {
        binding.layoutReminderTime.setOnClickListener {
            showTimePickerDialog()
        }
    }

    /**
     * Load current settings and update UI
     */
    private fun loadCurrentSettings() {
        val context = requireContext()

        // App notifications
        val notificationsEnabled = UserPreferences.areNotificationsEnabled(context)
        binding.switchNotifications.isChecked = notificationsEnabled

        // Reminder settings
        val reminderEnabled = UserPreferences.isReminderEnabled(context)
        binding.switchReminder.isChecked = reminderEnabled
        binding.switchReminder.isEnabled = notificationsEnabled

        // Update reminder time display
        updateReminderTimeDisplay()

        // Show/hide time picker row based on reminder state
        updateReminderTimeVisibility(reminderEnabled)
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for older versions
        }
    }

    /**
     * Request notification permission for Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show rationale dialog
                showPermissionRationaleDialog()
            } else {
                // Request permission directly
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Permission not needed for older versions
            enableReminder()
        }
    }

    /**
     * Show dialog explaining why notification permission is needed
     */
    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.notifications)
            .setMessage(R.string.reminder_permission_required)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                binding.switchReminder.isChecked = false
            }
            .show()
    }

    /**
     * Show dialog when permission is denied
     */
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.notifications)
            .setMessage(R.string.reminder_permission_denied)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Open app settings for manual permission grant
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    /**
     * Enable the daily reminder
     */
    private fun enableReminder() {
        val context = requireContext()
        
        // Save preference
        UserPreferences.setReminderEnabled(context, true)

        // Get current reminder time
        val hour = UserPreferences.getReminderHour(context)
        val minute = UserPreferences.getReminderMinute(context)

        // Schedule the reminder
        ReminderScheduler.scheduleDailyReminder(context, hour, minute)

        // Update UI
        updateReminderTimeVisibility(true)

        // Show confirmation
        val timeStr = UserPreferences.getFormattedReminderTime(context)
        Toast.makeText(
            context,
            getString(R.string.reminder_enabled_message, timeStr),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Disable the daily reminder
     */
    private fun disableReminder() {
        val context = requireContext()

        // Save preference
        UserPreferences.setReminderEnabled(context, false)

        // Cancel the scheduled reminder
        ReminderScheduler.cancelDailyReminder(context)

        // Update UI
        updateReminderTimeVisibility(false)

        // Show confirmation
        Toast.makeText(
            context,
            R.string.reminder_disabled_message,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Show time picker dialog
     */
    private fun showTimePickerDialog() {
        val context = requireContext()
        val currentHour = UserPreferences.getReminderHour(context)
        val currentMinute = UserPreferences.getReminderMinute(context)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            currentHour,
            currentMinute,
            true // 24-hour format
        ).show()
    }

    /**
     * Handle time selection from picker
     */
    private fun onTimeSelected(hour: Int, minute: Int) {
        val context = requireContext()

        // Save the new time
        UserPreferences.setReminderTime(context, hour, minute)

        // Update the display
        updateReminderTimeDisplay()

        // Reschedule the reminder with new time
        if (UserPreferences.isReminderEnabled(context)) {
            ReminderScheduler.updateReminderTime(context, hour, minute)

            // Show confirmation
            val timeStr = UserPreferences.getFormattedReminderTime(context)
            Toast.makeText(
                context,
                getString(R.string.reminder_enabled_message, timeStr),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Update the reminder time display
     */
    private fun updateReminderTimeDisplay() {
        binding.tvReminderTime.text = UserPreferences.getFormattedReminderTime(requireContext())
    }

    /**
     * Show/hide the reminder time row based on reminder state
     */
    private fun updateReminderTimeVisibility(isEnabled: Boolean) {
        val visibility = if (isEnabled) View.VISIBLE else View.GONE
        binding.reminderDivider.visibility = visibility
        binding.layoutReminderTime.visibility = visibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
