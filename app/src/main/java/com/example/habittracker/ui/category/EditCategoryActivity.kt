package com.example.habittracker.ui.category

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityEditCategoryBinding
import com.example.habittracker.ui.main.MainActivity

class EditCategoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditCategoryActivity"
    }

    private lateinit var binding: ActivityEditCategoryBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MainActivity.hideSystemUI(this)
        applyWindowInsets()
        setupClickListeners()

        // Debug: Log all incoming intent extras
        Log.d(TAG, "onCreate - Intent extras: ${intent.extras}")
        Log.d(TAG, "category_id from intent: ${intent.getStringExtra("category_id")}")
        Log.d(TAG, "category_name from intent: ${intent.getStringExtra("category_name")}")
        Log.d(TAG, "category_icon from intent: ${intent.getIntExtra("category_icon", -1)}")
        Log.d(TAG, "category_background from intent: ${intent.getIntExtra("category_background", -1)}")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Create a bundle with the correct argument keys for Navigation Component
        val args = Bundle().apply {
            intent.getStringExtra("category_id")?.let {
                putString("category_id", it)
                Log.d(TAG, "Added category_id to args: $it")
            } ?: Log.e(TAG, "category_id is NULL in intent!")

            intent.getStringExtra("category_name")?.let {
                putString("category_name", it)
            }
            putInt("category_icon", intent.getIntExtra("category_icon", 0))
            putInt("category_background", intent.getIntExtra("category_background", 0))
        }

        // Manually set the graph with arguments
        navController.setGraph(R.navigation.nav_edit_category, args)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBarsInsets.top)
            windowInsets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            MainActivity.hideSystemUI(this)
        }
    }
}
