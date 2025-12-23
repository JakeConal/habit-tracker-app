package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentCategoryBinding

/**
 * CategoryFragment - Screen for selecting a category
 */
class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var categoryAdapter: CategoryAdapter
    
    // Categories with their corresponding icons
    private val categories = listOf(
        Category("Reading", R.drawable.ic_book, "#FF6B6B"),
        Category("Exercise", R.drawable.ic_fitness, "#4ECDC4"),
        Category("Study", R.drawable.ic_book, "#45B7D1"),
        Category("Work", R.drawable.ic_work, "#FFA07A"),
        Category("Health", R.drawable.ic_health, "#98D8C8"),
        Category("Meditation", R.drawable.ic_meditation, "#C7CEEA"),
        Category("Cooking", R.drawable.ic_other, "#FFD93D"),
        Category("Music", R.drawable.ic_other, "#FF85A2"),
        Category("Art", R.drawable.ic_other, "#95E1D3"),
        Category("Writing", R.drawable.ic_book, "#F38181"),
        Category("Language", R.drawable.ic_book, "#AA96DA"),
        Category("Other", R.drawable.ic_other, "#FCBAD3")
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
        binding.tvTitle.text = "Select Category"
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { category ->
            onCategorySelected(category)
        }
        
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onCategorySelected(category: Category) {
        // Send result back to CreateHabitFragment
        setFragmentResult(
            "category_request_key",
            bundleOf(
                "selected_category_name" to category.name,
                "selected_category_icon" to category.iconRes
            )
        )
        
        // Navigate back
        findNavController().navigateUp()
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
    val color: String
)
