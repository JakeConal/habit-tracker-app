package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentCategoryBinding

/**
 * CategoryFragment - Screen for managing categories
 */
class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var categoryAdapter: CategoryAdapter
    
    // Categories matching the Figma design
    private val categories = listOf(
        Category("Physical Health", R.drawable.ic_heart, R.drawable.bg_category_icon_red, 5),
        Category("Study", R.drawable.ic_book, R.drawable.bg_category_icon_blue, 3),
        Category("Finance", R.drawable.ic_money, R.drawable.bg_category_icon_yellow, 2),
        Category("Mental Health", R.drawable.ic_heart, R.drawable.bg_category_icon_pink_light, 4),
        Category("Career", R.drawable.ic_briefcase, R.drawable.bg_category_icon_purple, 3),
        Category("Nutrition", R.drawable.ic_food, R.drawable.bg_category_icon_orange_light, 6),
        Category("Personal Growth", R.drawable.ic_growth, R.drawable.bg_category_icon_green, 4),
        Category("Sleep", R.drawable.ic_moon, R.drawable.bg_category_icon_indigo, 2)
    )

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
    }

    private fun setupView() {
        binding.tvTitle.text = "Manage Categories"
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = categories,
            onCategoryClick = { category ->
                // Send selected category back to CreateHabitFragment
                setFragmentResult(
                    "category_request_key",
                    bundleOf(
                        "selected_category_name" to category.name,
                        "selected_category_icon" to category.iconRes,
                        "selected_category_icon_background" to category.backgroundRes
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class for Category
data class Category(
    val name: String,
    val iconRes: Int,
    val backgroundRes: Int,
    val habitCount: Int
)
