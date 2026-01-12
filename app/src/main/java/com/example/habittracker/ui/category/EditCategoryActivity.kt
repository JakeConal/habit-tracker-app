package com.example.habittracker.ui.category

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityEditCategoryBinding

class EditCategoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditCategoryActivity"
    }

    private lateinit var binding: ActivityEditCategoryBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        
        Log.d(TAG, "Final args bundle: $args")
        Log.d(TAG, "category_id in args: ${args.getString("category_id")}")
        
        // Pass the bundle to the start destination (fragment)
        navController.setGraph(R.navigation.nav_edit_category, args)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

