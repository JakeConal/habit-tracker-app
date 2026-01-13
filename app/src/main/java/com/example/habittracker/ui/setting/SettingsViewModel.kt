package com.example.habittracker.ui.setting

import androidx.lifecycle.ViewModel
import com.example.habittracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingMenuItem(
    val id: Int,
    val titleRes: Int,
    val iconRes: Int
)

class SettingsViewModel : ViewModel() {

    private val _settingsMenuItems = MutableStateFlow<List<SettingMenuItem>>(emptyList())
    val settingsMenuItems: StateFlow<List<SettingMenuItem>> = _settingsMenuItems.asStateFlow()

    init {
        loadMenuItems()
    }

    private fun loadMenuItems() {
        _settingsMenuItems.value = listOf(
            SettingMenuItem(
                id = 1,
                titleRes = R.string.settings_edit_profile,
                iconRes = R.drawable.ic_person
            ),
            SettingMenuItem(
                id = 2,
                titleRes = R.string.settings_reset_password,
                iconRes = R.drawable.ic_lock
            ),
            SettingMenuItem(
                id = 3,
                titleRes = R.string.settings_notification,
                iconRes = R.drawable.ic_notification_menu
            ),
            SettingMenuItem(
                id = 4,
                titleRes = R.string.settings_terms,
                iconRes = R.drawable.ic_terms
            ),
            SettingMenuItem(
                id = 5,
                titleRes = R.string.settings_review_challenge,
                iconRes = R.drawable.ic_verified_badge
            ),
            SettingMenuItem(
                id = 6,
                titleRes = R.string.settings_logout,
                iconRes = R.drawable.ic_settings_general
            ),
            SettingMenuItem(
                id = 7,
                titleRes = R.string.settings_delete_account,
                iconRes = R.drawable.ic_trash
            )
        )
    }

    fun onMenuItemClick(menuItemId: Int) {
        // Handle navigation based on menu item ID
        // This will be handled by the Fragment using Navigation Component
    }
}
