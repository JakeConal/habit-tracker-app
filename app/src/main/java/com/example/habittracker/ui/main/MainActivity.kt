package com.example.habittracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.social.CommunityActivity

/**
 * MainActivity - Container chính của ứng dụng
 * Quản lý Navigation Component và BottomNavigationView
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWindowInsets()
        setupNavigation()
        setupBottomNavigation()
        setupFab()
    }

    /**
     * Setup window insets để hỗ trợ edge-to-edge display
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
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
     * Xử lý đặc biệt cho Community tab (mở CommunityActivity)
     */
    private fun setupBottomNavigation() {
        // Kết nối BottomNavigation với NavController cho các tab cơ bản
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Xử lý riêng cho Community tab vì nó mở Activity khác
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                    true
                }
                R.id.nav_statistic -> {
                    navController.navigate(R.id.nav_statistic)
                    true
                }
                R.id.nav_community -> {
                    // Mở CommunityActivity thay vì navigate đến fragment
                    openCommunityScreen()
                    false // Return false để không thay đổi selection state
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.nav_profile)
                    true
                }
                R.id.nav_placeholder -> {
                    // Placeholder cho FAB, không làm gì
                    false
                }
                else -> false
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
     * Mở CommunityActivity
     */
    private fun openCommunityScreen() {
        val intent = Intent(this, CommunityActivity::class.java)
        startActivity(intent)
    }

    /**
     * Xử lý back navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
