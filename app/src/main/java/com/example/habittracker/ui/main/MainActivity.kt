package com.example.habittracker.ui.main

import android.Manifest
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
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.example.habittracker.data.model.User
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.NotificationRepository
import com.example.habittracker.data.repository.FirestoreUserRepository
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.feed.CommentsActivity
import com.example.habittracker.util.NotificationHelper
import com.example.habittracker.util.UserPreferences
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.collectLatest
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

        // Initialize current user to trigger notification listener and other user-dependent logic
        lifecycleScope.launch {
            FirestoreUserRepository.getInstance().getCurrentUser()
        }

        // Ask for permission after UI is ready
        binding.root.post {
            askNotificationPermission()
            updateFcmToken()
        }

        setupNotificationListener()

        // Handle navigation from notification if present in starting intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI(this)

        // Ensure token is fresh when user returns to app
        if (AuthRepository.getInstance().isUserLoggedIn()) {
            updateFcmToken()
        }
    }

    private fun handleIntent(intent: Intent?) {
        val postId = intent?.getStringExtra("postId")
        if (!postId.isNullOrEmpty()) {
            navigateToPost(postId)
        }
    }

    private fun navigateToPost(postId: String) {
        lifecycleScope.launch {
            try {
                val post = PostRepository.getInstance().getPostById(postId)
                if (post != null) {
                    val intent = Intent(this@MainActivity, CommentsActivity::class.java).apply {
                        putExtra(CommentsActivity.EXTRA_POST_ID, post.id)
                        putExtra(CommentsActivity.EXTRA_POST_USER_ID, post.userId)
                        putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
                        putExtra(CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatarUrl)
                        putExtra(CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
                        putExtra(CommentsActivity.EXTRA_CONTENT, post.content)
                        putExtra(CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
                        putExtra(CommentsActivity.EXTRA_LIKES_COUNT, post.likeCount)
                        putExtra(CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentCount)
                        putExtra(CommentsActivity.EXTRA_IS_LIKED, post.likedBy.contains(AuthRepository.getInstance().getCurrentUser()?.uid))

                        if (!post.originalPostId.isNullOrEmpty()) {
                            putExtra(CommentsActivity.EXTRA_ORIGINAL_POST_ID, post.originalPostId)
                            putExtra(CommentsActivity.EXTRA_ORIGINAL_USER_ID, post.originalUserId)
                            putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_NAME, post.originalAuthorName)
                            putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_AVATAR, post.originalAuthorAvatarUrl)
                            putExtra(CommentsActivity.EXTRA_ORIGINAL_CONTENT, post.originalContent)
                            putExtra(CommentsActivity.EXTRA_ORIGINAL_IMAGE_URL, post.originalImageUrl)
                        }
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                // Use flatMapLatest or just nested collection properly
                FirestoreUserRepository.getInstance().currentUser.collectLatest { user: User? ->
                    if (user != null) {
                        android.util.Log.d("NotificationListener", "Starting listener for user: ${user.id}")
                        try {
                            NotificationRepository.getInstance()
                                .getNewNotifications(user.id)
                                .collect { notification ->
                                    android.util.Log.d("NotificationListener", "Received notification: ${notification.id}")
                                    if (UserPreferences.areNotificationsEnabled(this@MainActivity)) {
                                        showInAppNotification(notification)
                                    }
                                }
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationListener", "Inner error: ${e.message}")
                        }
                    } else {
                        android.util.Log.d("NotificationListener", "No user logged in, listener idle")
                    }
                }
            }
        }
    }

    private fun showInAppNotification(notification: Notification) {
        // Build specific text for in-app or system notification trigger if desired
        val text = when(notification.type) {
            Notification.NotificationType.LIKE_POST -> "${notification.senderName} liked your post"
            Notification.NotificationType.COMMENT_POST -> "${notification.senderName} commented on your post"
            Notification.NotificationType.SHARE_POST -> "${notification.senderName} shared your post"
            Notification.NotificationType.REPLY_COMMENT -> "${notification.senderName} replied to your comment"
            Notification.NotificationType.LIKE_COMMENT -> "${notification.senderName} liked your comment"
            Notification.NotificationType.DISLIKE_COMMENT -> "${notification.senderName} disliked your comment"
        }

        try {
            // Trigger system notification - title is generic to avoid duplication
            triggerSystemNotification("Habit Tracker Interaction", text, notification.postId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerSystemNotification(title: String, messageBody: String, postId: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (postId != null) {
                putExtra("postId", postId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = NotificationHelper.DEFAULT_CHANNEL_ID
        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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

        // Check if user is already logged in to skip login screen
        if (AuthRepository.getInstance().isUserLoggedIn()) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_main)
            navGraph.setStartDestination(R.id.nav_home)
            navController.graph = navGraph
        }

        // Hide bottom navigation and FAB on authentication screens and create/select screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val layoutParams = binding.navHostFragment.layoutParams as ConstraintLayout.LayoutParams
            when (destination.id) {
                R.id.nav_login, 
                R.id.nav_register,
                R.id.nav_forgot_password,
                R.id.nav_create_habit,
                R.id.nav_create_category,
                R.id.nav_view_habit,
                R.id.nav_focus_timer,
                R.id.nav_terms -> {
                    binding.bottomNavigationContainer.visibility = View.GONE
                    layoutParams.bottomToTop = -1
                    layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                else -> {
                    binding.bottomNavigationContainer.visibility = View.VISIBLE
                    layoutParams.bottomToTop = R.id.bottomNavigationContainer
                    layoutParams.bottomToBottom = -1
                }
            }
            binding.navHostFragment.layoutParams = layoutParams
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
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android 11 and above
                    activity.window.decorView.post {
                        activity.window.insetsController?.let { controller ->
                            controller.hide(WindowInsets.Type.navigationBars())
                            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
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
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error hiding system UI: ${e.message}")
            }
        }
    }
}
