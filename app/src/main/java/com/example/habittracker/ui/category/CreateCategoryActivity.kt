package com.example.habittracker.ui.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityCreateCategoryBinding

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCategoryBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Pass intent extras to the start destination (fragment)
        navController.setGraph(R.navigation.nav_create_category, intent.extras)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
