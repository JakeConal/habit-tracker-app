package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.CategoryColor
import com.example.habittracker.data.model.CategoryIcon
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.databinding.FragmentCategoryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * CategoryFragment - Screen for managing categories
 */
class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CategoryViewModel by viewModels()
    
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        applyWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        setupFragmentResultListener()
        observeData()
    }

    private fun setupView() {
        binding.tvTitle.text = getString(R.string.manage_categories)
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
                // Send selected category back to CreateHabitFragment
                setFragmentResult(
                    "category_request_key",
                    bundleOf(
                        "selected_category_name" to category.title,
                        "selected_category_icon" to category.icon.resId,
                        "selected_category_icon_background" to category.color.resId,
                        "selected_category_id" to category.id
                    )
                )
                findNavController().navigateUp()
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
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.fabAddCategory.setOnClickListener {
            findNavController().navigate(R.id.action_category_to_create_category)
        }
    }
    
    private fun observeData() {
        // Observe categories list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categoriesList ->
                categoryAdapter.updateCategories(categoriesList.toMutableList())
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { _ ->
                // You can show/hide loading indicator here if needed
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    showError(it)
                }
            }
        }

        // Observe category added
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryAdded.collect { success ->
                if (success) {
                    showSuccess("Category added successfully")
                    binding.rvCategories.scrollToPosition(0)
                }
            }
        }

        // Observe category updated
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryUpdated.collect { success ->
                if (success) {
                    showSuccess("Category updated successfully")
                }
            }
        }

        // Observe category deleted
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryDeleted.collect { success ->
                if (success) {
                    showSuccess("Category deleted successfully")
                }
            }
        }
    }

    private fun setupFragmentResultListener() {
        // Listen for new category created from CreateCategoryFragment
        parentFragmentManager.setFragmentResultListener(
            "new_category_request_key",
            viewLifecycleOwner
        ) { _, bundle ->
            val categoryName = bundle.getString("category_name")
            val categoryIcon = bundle.getInt("category_icon")
            val categoryBackground = bundle.getInt("category_background")
            
            if (categoryName != null && categoryIcon != 0 && categoryBackground != 0) {
                val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return@setFragmentResultListener
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

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
