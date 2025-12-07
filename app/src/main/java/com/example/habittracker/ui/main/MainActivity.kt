package com.example.habittracker.ui.main

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityMainBinding

/**
 * MainActivity - Container chính của ứng dụng
 * Quản lý Navigation Component và BottomNavigationView
 * Sử dụng Single-Activity Architecture với các Fragments
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Cho phép content vẽ phía sau system bars (không chiếm diện tích)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWindowInsets()
        setupNavigation()
        setupBottomNavigation()
        setupFab()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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
            // TODO: Mở màn hình thêm habit mới
            Toast.makeText(this, "Add new habit", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Xử lý back navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
