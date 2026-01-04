package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.CategoryColor
import com.example.habittracker.data.model.CategoryIcon
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

/**
 * CategoryFragment - Screen for managing categories
 */
class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var categoryAdapter: CategoryAdapter
    
    private val categories = mutableListOf<Category>()

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
        setupRecyclerView()
        setupClickListeners()
        setupFragmentResultListener()
        loadUserCategories()
    }

    private fun setupView() {
        binding.tvTitle.text = getString(R.string.manage_categories)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = categories,
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
                // TODO: Handle edit category
            },
            onDeleteClick = { category ->
                // TODO: Handle delete category
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
                lifecycleScope.launch {
                    val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return@launch
                    val iconEnum = CategoryIcon.entries.firstOrNull { it.resId == categoryIcon } ?: CategoryIcon.HEART
                    val colorEnum = CategoryColor.entries.firstOrNull { it.resId == categoryBackground } ?: CategoryColor.RED
                    val categoryModel = Category(
                        userId = userId,
                        title = categoryName,
                        icon = iconEnum,
                        color = colorEnum
                    )
                    CategoryRepository.getInstance().addCategory(categoryModel)
                    // Add to local list
                    val newCategory = Category(
                        title = categoryName,
                        icon = iconEnum,
                        color = colorEnum,
                        habitCount = 0
                    )
                    categories.add(0, newCategory)
                    categoryAdapter.notifyItemInserted(0)
                    binding.rvCategories.scrollToPosition(0)
                }
            }
        }
    }

    private fun loadUserCategories() {
        lifecycleScope.launch {
            val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return@launch
            val userCategories = CategoryRepository.getInstance().getCategoriesForUser(userId)
            for (cat in userCategories) {
                if (categories.none { it.title == cat.title }) {
                    categories.add(Category(title = cat.title, icon = cat.icon, color = cat.color, habitCount = 0))
                }
            }
            categoryAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
