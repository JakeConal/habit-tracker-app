package com.example.habittracker.ui.category

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.CategoryColor
import com.example.habittracker.data.model.CategoryIcon
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.databinding.ActivityCategoryBinding
import com.example.habittracker.ui.habit.add.CreateHabitActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * CategoryActivity - Activity for managing categories
 */
class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    companion object {
        private const val REQUEST_CODE_CREATE_CATEGORY = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupView()
        applyWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupView() {
        binding.tvTitle.text = getString(R.string.manage_categories)
        
        // Enable edge-to-edge
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun applyWindowInsets() {
        // Handle edge-to-edge and window insets
        
        // Root container handles top inset (status bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding top for status bar
            view.updatePadding(
                top = systemBarsInsets.top
            )
            
            // Pass insets down to child views
            windowInsets
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = mutableListOf(),
            onCategoryClick = { category ->
                // Send selected category back to calling activity
                val resultIntent = Intent().apply {
                    putExtra(CreateHabitActivity.EXTRA_CATEGORY_ID, category.id)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            },
            onEditClick = { category ->
                // Handle edit category
                viewModel.updateCategory(category)
            },
            onDeleteClick = { category ->
                // Handle delete category
                viewModel.deleteCategory(category.id)
            }
        )
        
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.fabAddCategory.setOnClickListener {
            navigateToCreateCategory()
        }
    }
    
    private fun observeData() {
        // Observe categories list
        lifecycleScope.launch {
            viewModel.categories.collect { categoriesList ->
                categoryAdapter.updateCategories(categoriesList.toMutableList())
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { _ ->
                // You can show/hide loading indicator here if needed
            }
        }

        // Observe errors
        lifecycleScope.launch {
            viewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    showError(it)
                }
            }
        }

        // Observe category added
        lifecycleScope.launch {
            viewModel.categoryAdded.collect { category ->
                if (category != null) {
                    showSuccess("Category added successfully")
                    binding.rvCategories.scrollToPosition(0)
                    // Automatically send the new category back
                    sendCategoryResult(category)
                }
            }
        }

        // Observe category updated
        lifecycleScope.launch {
            viewModel.categoryUpdated.collect { category ->
                if (category != null) {
                    showSuccess("Category updated successfully")
                    // Automatically send the updated category back
                    sendCategoryResult(category)
                }
            }
        }

        // Observe category deleted
        lifecycleScope.launch {
            viewModel.categoryDeleted.collect { success ->
                if (success) {
                    showSuccess("Category deleted successfully")
                }
            }
        }
    }

    private fun navigateToCreateCategory() {
        // TODO: Start CreateCategoryActivity when it's created
        // For now, just show a placeholder message
        showError("Create Category screen not implemented yet")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREATE_CATEGORY && resultCode == RESULT_OK) {
            val categoryName = data?.getStringExtra("category_name")
            val categoryIcon = data?.getIntExtra("category_icon", 0) ?: 0
            val categoryBackground = data?.getIntExtra("category_background", 0) ?: 0
            
            if (categoryName != null && categoryIcon != 0 && categoryBackground != 0) {
                val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return
                val iconEnum = CategoryIcon.entries.firstOrNull { it.resId == categoryIcon } ?: CategoryIcon.HEART
                val colorEnum = CategoryColor.entries.firstOrNull { it.resId == categoryBackground } ?: CategoryColor.RED
                val categoryModel = Category(
                    userId = userId,
                    title = categoryName,
                    icon = iconEnum,
                    color = colorEnum
                )
                viewModel.addCategory(categoryModel)
            }
        }
    }

    private fun sendCategoryResult(category: Category) {
        val resultIntent = Intent().apply {
            putExtra(CreateHabitActivity.EXTRA_CATEGORY_ID, category.id)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
