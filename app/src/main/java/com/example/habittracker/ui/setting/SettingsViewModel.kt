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
                titleRes = R.string.settings_general,
                iconRes = R.drawable.ic_settings_general
            ),
            SettingMenuItem(
                id = 2,
                titleRes = R.string.settings_notification,
                iconRes = R.drawable.ic_notification_menu
            ),
            SettingMenuItem(
                id = 3,
                titleRes = R.string.settings_subscription,
                iconRes = R.drawable.ic_subscription
            ),
            SettingMenuItem(
                id = 4,
                titleRes = R.string.settings_language,
                iconRes = R.drawable.ic_language
            ),
            SettingMenuItem(
                id = 5,
                titleRes = R.string.settings_report,
                iconRes = R.drawable.ic_report
            ),
            SettingMenuItem(
                id = 6,
                titleRes = R.string.settings_terms,
                iconRes = R.drawable.ic_terms
            ),
            SettingMenuItem(
                id = 7,
                titleRes = R.string.settings_support,
                iconRes = R.drawable.ic_support
            )
        )
    }

    fun onMenuItemClick(menuItemId: Int) {
        // Handle navigation based on menu item ID
        // This will be handled by the Fragment using Navigation Component
    }
}
