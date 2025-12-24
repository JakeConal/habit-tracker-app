package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentCreateCategoryBinding

/**
 * CreateCategoryFragment - Screen for creating a new category
 */
class CreateCategoryFragment : Fragment() {

    private var _binding: FragmentCreateCategoryBinding? = null
    private val binding get() = _binding!!
    
    private var selectedColorRes: Int = R.drawable.bg_category_icon_purple
    private var selectedIconRes: Int = R.drawable.ic_book
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupClickListeners()
    }

    private fun setupView() {
        // Set default preview values
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
        
        binding.btnCreate.setOnClickListener {
            createCategory()
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

    private fun createCategory() {
        val categoryTitle = binding.etCategoryTitle.text.toString().trim()
        
        if (categoryTitle.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter a category name",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // TODO: Save category to database or ViewModel
        Toast.makeText(
            requireContext(),
            "Category '$categoryTitle' created successfully",
            Toast.LENGTH_SHORT
        ).show()
        
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ColorOption(
    val name: String,
    val backgroundRes: Int
)

data class IconOption(
    val name: String,
    val iconRes: Int
)
