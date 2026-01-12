package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.CategoryColor
import com.example.habittracker.data.model.CategoryIcon
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.databinding.FragmentEditCategoryBinding
import kotlinx.coroutines.launch

/**
 * EditCategoryFragment - Screen for editing an existing category
 * Uses fragment_edit_category.xml layout
 */
class EditCategoryFragment : Fragment() {

    companion object {
        const val RESULT_CATEGORY_UPDATED = "result_category_updated"
        
        // Argument keys
        const val ARG_CATEGORY_ID = "category_id"
        const val ARG_CATEGORY_NAME = "category_name"
        const val ARG_CATEGORY_ICON = "category_icon"
        const val ARG_CATEGORY_BACKGROUND = "category_background"
    }

    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels()
    
    private var selectedColorRes: Int = R.drawable.bg_category_icon_purple
    private var selectedIconRes: Int = R.drawable.ic_book
    
    // Edit category data
    private lateinit var categoryId: String
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadArguments()
        setupView()
        setupClickListeners()
        observeData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryUpdated.collect { category ->
                if (category != null) {
                    val bundle = bundleOf(
                        "category_id" to category.id,
                        "category_name" to category.title,
                        "category_icon" to category.icon.resId,
                        "category_background" to category.color.resId
                    )
                    setFragmentResult(RESULT_CATEGORY_UPDATED, bundle)
                    
                    Toast.makeText(requireContext(), "Category updated successfully", Toast.LENGTH_SHORT).show()
                    if (!findNavController().navigateUp()) {
                        requireActivity().finish()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadArguments() {
        arguments?.let { bundle ->
            categoryId = bundle.getString(ARG_CATEGORY_ID) 
                ?: throw IllegalArgumentException("category_id is required for EditCategoryFragment")
            
            val categoryName = bundle.getString(ARG_CATEGORY_NAME) ?: ""
            val categoryIcon = bundle.getInt(ARG_CATEGORY_ICON, R.drawable.ic_book)
            val categoryBackground = bundle.getInt(ARG_CATEGORY_BACKGROUND, R.drawable.bg_category_icon_purple)
            
            // Prefill the values
            selectedIconRes = categoryIcon
            selectedColorRes = categoryBackground
            binding.etCategoryTitle.setText(categoryName)
        } ?: throw IllegalArgumentException("Arguments are required for EditCategoryFragment")
    }

    private fun setupView() {
        // Set title for edit mode
        binding.tvTitleText.text = "Edit Category"
        
        // Set button text for edit mode
        binding.tvCreateButtonText.text = "Update"
        
        // Set preview values from arguments
        binding.viewColorPreview.setBackgroundResource(selectedColorRes)
        binding.ivIconPreview.setImageResource(selectedIconRes)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnSelectColor.setOnClickListener {
            showColorPickerDialog()
        }
        
        binding.btnSelectIcon.setOnClickListener {
            showIconPickerDialog()
        }
        
        binding.btnUpdate.setOnClickListener {
            updateCategory()
        }
    }

    private fun showColorPickerDialog() {
        val colors = listOf(
            ColorOption("Purple", R.drawable.bg_category_icon_purple),
            ColorOption("Blue", R.drawable.bg_category_icon_blue),
            ColorOption("Green", R.drawable.bg_category_icon_green),
            ColorOption("Red", R.drawable.bg_category_icon_red),
            ColorOption("Yellow", R.drawable.bg_category_icon_yellow),
            ColorOption("Pink", R.drawable.bg_category_icon_pink_light),
            ColorOption("Orange", R.drawable.bg_category_icon_orange_light),
            ColorOption("Indigo", R.drawable.bg_category_icon_indigo)
        )
        
        ColorPickerDialog.show(
            parentFragmentManager,
            colors
        ) { selectedColor ->
            selectedColorRes = selectedColor.backgroundRes
            binding.viewColorPreview.setBackgroundResource(selectedColorRes)
        }
    }

    private fun showIconPickerDialog() {
        val icons = listOf(
            IconOption("Book", R.drawable.ic_book),
            IconOption("Heart", R.drawable.ic_heart),
            IconOption("Briefcase", R.drawable.ic_briefcase),
            IconOption("Food", R.drawable.ic_food),
            IconOption("Growth", R.drawable.ic_growth),
            IconOption("Moon", R.drawable.ic_moon),
            IconOption("Money", R.drawable.ic_money),
            IconOption("Fitness", R.drawable.ic_fitness),
            IconOption("Health", R.drawable.ic_health),
            IconOption("Water", R.drawable.ic_water),
            IconOption("Walk", R.drawable.ic_walk),
            IconOption("Meditation", R.drawable.ic_meditation),
            IconOption("Work", R.drawable.ic_work),
            IconOption("Other", R.drawable.ic_other)
        )
        
        IconPickerDialog.show(
            parentFragmentManager,
            icons
        ) { selectedIcon ->
            selectedIconRes = selectedIcon.iconRes
            binding.ivIconPreview.setImageResource(selectedIconRes)
        }
    }

    private fun updateCategory() {
        val categoryTitle = binding.etCategoryTitle.text.toString().trim()
        
        if (categoryTitle.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return
        val iconEnum = CategoryIcon.entries.firstOrNull { it.resId == selectedIconRes } ?: CategoryIcon.HEART
        val colorEnum = CategoryColor.entries.firstOrNull { it.resId == selectedColorRes } ?: CategoryColor.RED
        
        val category = Category(
            id = categoryId,
            userId = userId,
            title = categoryTitle,
            icon = iconEnum,
            color = colorEnum
        )
        
        viewModel.updateCategory(category)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
