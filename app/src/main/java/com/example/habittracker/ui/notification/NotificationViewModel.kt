package com.example.habittracker.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Notification
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val authRepository = AuthRepository.getInstance()
    private val firestoreUserRepository = FirestoreUserRepository.getInstance()
    private val notificationRepository = NotificationRepository.getInstance()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _showAllNotifications = MutableStateFlow(false)
    val showAllNotifications: StateFlow<Boolean> = _showAllNotifications.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            firestoreUserRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    loadNotifications(user.id)
                } else {
                    _notifications.value = emptyList()
                    _unreadCount.value = 0
                }
            }
        }
    }

    private suspend fun loadNotifications(userId: String) {
        try {
            notificationRepository.getNotifications(userId).collect { list ->
                _notifications.value = list
                _unreadCount.value = list.count { !it.read }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationViewModel", "Error loading notifications: ${e.message}")
        }
    }

    fun setShowAllNotifications(show: Boolean) {
        _showAllNotifications.value = show
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }
}
