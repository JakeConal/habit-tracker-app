package com.example.habittracker.ui.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.habittracker.R
import com.example.habittracker.data.model.Notification
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.NotificationRepository
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.utils.UserPreferences
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

/**
 * MainActivity - Container chính của ứng dụng
 * Quản lý Navigation Component và BottomNavigationView
 * Sử dụng Single-Activity Architecture với các Fragments
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
        } else {
            // Permission is denied.
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWindowInsets()
        setupNavigation()
        setupBottomNavigation()
        setupFab()
        hideSystemUI(this)

        // Ask for permission after UI is ready
        binding.root.post {
            askNotificationPermission()
            updateFcmToken()
        }

        setupNotificationListener()
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            val user = AuthRepository.getInstance().getCurrentUser()
            if (user != null) {
                lifecycleScope.launch {
                    FirestoreUserRepository.getInstance().updateFcmToken(user.uid, token)
                }
            }
        }
    }

    private fun setupNotificationListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    val user = AuthRepository.getInstance().getCurrentUser()
                    if (user != null) {
                        android.util.Log.d("NotificationListener", "Starting listener for user: ${user.uid}")
                        NotificationRepository.getInstance()
                            .getNewNotifications(user.uid)
                            .collect { notification ->
                                android.util.Log.d("NotificationListener", "Received notification: ${notification.id}")
                                if (UserPreferences.areNotificationsEnabled(this@MainActivity)) {
                                    showInAppNotification(notification)
                                }
                            }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotificationListener", "Error: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showInAppNotification(notification: Notification) {
        // Show Snackbar for immediate In-App feedback
        val text = when(notification.type) {
            Notification.NotificationType.LIKE_POST -> "${notification.senderName} liked your post"
            Notification.NotificationType.COMMENT_POST -> "${notification.senderName} commented on your post"
            Notification.NotificationType.SHARE_POST -> "${notification.senderName} shared your post"
            Notification.NotificationType.REPLY_COMMENT -> "${notification.senderName} replied to your comment"
            Notification.NotificationType.LIKE_COMMENT -> "${notification.senderName} liked your comment"
            Notification.NotificationType.DISLIKE_COMMENT -> "${notification.senderName} disliked your comment"
        }

        try {

            com.google.android.material.snackbar.Snackbar.make(binding.root, text, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fabAdd) // Avoid covering FAB
                .show()

            // Trigger system notification
            triggerSystemNotification(text, text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerSystemNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "habit_tracker_default_channel"
        val channelName = "Habit Tracker Notifications"
        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            insets
        }
    }



    /**
     * Setup Navigation Component với NavHostFragment
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Hide bottom navigation and FAB on authentication screens and create/select screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_login, 
                R.id.nav_register,
                R.id.nav_create_habit,
                R.id.nav_create_category,
                R.id.nav_view_habit,
                R.id.nav_focus_timer -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.fabAdd.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.fabAdd.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Setup BottomNavigationView với NavController
     * Tất cả các tabs giờ đều sử dụng Navigation Component
     */
    private fun setupBottomNavigation() {
        // Kết nối BottomNavigation với NavController
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Xử lý để skip placeholder item
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_placeholder -> {
                    // Placeholder cho FAB, không làm gì
                    false
                }
                else -> {
                    // Let NavigationUI handle navigation for other items
                    try {
                        if (navController.currentDestination?.id != item.itemId) {
                            navController.navigate(item.itemId)
                        }
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
            }
        }
    }

    /**
     * Setup Floating Action Button
     */
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // Start CreateHabitActivity instead of navigating to fragment
            val intent = Intent(this, com.example.habittracker.ui.habit.add.CreateHabitActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI(this)
        }
    }

    /**
     * Yêu cầu quyền thông báo trên Android 13+ (Tiramisu)
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        /**
         * Utility function to hide the system navigation bar (3-button navigation)
         * Creates a full-screen immersive experience
         */
        fun hideSystemUI(activity: AppCompatActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11 and above
                activity.window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // For Android 10 and below
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
            }
        }
    }
}
