package com.example.habittracker.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.R
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingMenuItem(
    val id: Int,
    val titleRes: Int,
    val iconRes: Int
)

class SettingsViewModel : ViewModel() {

    private val userRepository = FirestoreUserRepository.getInstance()
    private val _settingsMenuItems = MutableStateFlow<List<SettingMenuItem>>(getDefaultItems())
    val settingsMenuItems: StateFlow<List<SettingMenuItem>> = _settingsMenuItems.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            // First time fetch
            userRepository.getCurrentUser()

            // Then observe changes
            userRepository.currentUser.collect { user ->
                val isAdmin = user?.role == User.ROLE_ADMIN
                _settingsMenuItems.value = generateMenuItems(isAdmin)
            }
        }
    }

    private fun getDefaultItems() = generateMenuItems(false)

    private fun generateMenuItems(isAdmin: Boolean): List<SettingMenuItem> {
        return mutableListOf<SettingMenuItem>().apply {
            add(SettingMenuItem(1, R.string.settings_edit_profile, R.drawable.ic_person))
            add(SettingMenuItem(2, R.string.settings_reset_password, R.drawable.ic_lock))
            add(SettingMenuItem(3, R.string.settings_notification, R.drawable.ic_notification_menu))
            add(SettingMenuItem(4, R.string.settings_terms, R.drawable.ic_terms))

            if (isAdmin) {
                add(SettingMenuItem(5, R.string.settings_review_challenge, R.drawable.ic_verified_badge))
            }

            add(SettingMenuItem(6, R.string.settings_logout, R.drawable.ic_settings_general))
            add(SettingMenuItem(7, R.string.settings_delete_account, R.drawable.ic_trash))
        }
    }
}
