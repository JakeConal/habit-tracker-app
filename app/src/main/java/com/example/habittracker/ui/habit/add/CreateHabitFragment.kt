package com.example.habittracker.ui.habit.add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentCreateHabitBinding
import com.example.habittracker.ui.common.BaseFragment
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.util.formatFrequency
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * CreateHabitFragment - Screen for creating a new habit
 * Follows MVVM architecture pattern
 */
class CreateHabitFragment : BaseFragment<FragmentCreateHabitBinding>() {

    private val viewModel: CreateHabitViewModel by viewModels()
    
    private val measurements = listOf("Mins", "Hours", "Pages", "Times", "Km", "Miles")
    private val frequencies = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateHabitBinding {
        return FragmentCreateHabitBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupClickListeners()
        setupInitialValues()
        setupCategoryResultListener()
    }

    private fun setupCategoryResultListener() {
        // Listen for category selection result from CategoryFragment
        parentFragmentManager.setFragmentResultListener(
            "category_request_key",
            viewLifecycleOwner
        ) { _, bundle ->
            val categoryName = bundle.getString("selected_category_name")
            val categoryIcon = bundle.getInt("selected_category_icon")
            val categoryIconBackground = bundle.getInt("selected_category_icon_background")
            val categoryId = bundle.getString("selected_category_id")

            if (categoryName != null && categoryIcon != 0 && categoryId != null) {
                binding.ivCategoryIcon.setImageResource(categoryIcon)
                binding.tvCategoryName.text = categoryName
                binding.categoryIconBackground.setCardBackgroundColor(
                    requireContext().getColor(
                        when (categoryIconBackground) {
                            R.drawable.bg_category_icon_red -> R.color.icon_bg_red
                            R.drawable.bg_category_icon_blue -> R.color.icon_bg_blue
                            R.drawable.bg_category_icon_yellow -> R.color.icon_bg_yellow
                            R.drawable.bg_category_icon_pink_light -> R.color.icon_bg_pink_light
                            R.drawable.bg_category_icon_purple -> R.color.icon_bg_purple
                            R.drawable.bg_category_icon_orange_light -> R.color.icon_bg_orange_light
                            R.drawable.bg_category_icon_green -> R.color.icon_bg_green
                            R.drawable.bg_category_icon_indigo -> R.color.icon_bg_indigo
                            else -> R.color.icon_bg_pink
                        }
                    )
                )
                viewModel.updateCategory(categoryName)
                viewModel.updateCategoryId(categoryId)
            }
        }
    }

    override fun observeData() {
        // Observe habit creation success
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitCreated.collect { success ->
                if (success) {
                    showSuccess("Habit created successfully!")
                    findNavController().navigateUp()
                }
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

        // Observe frequency changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.frequency.collect { frequency ->
                binding.tvFrequency.text = frequency.formatFrequency()
            }
        }

        // Observe time changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.time.collect { time ->
                binding.tvTime.text = time
            }
        }
    }

    private fun setupInitialValues() {
        // Load default category from repository
        lifecycleScope.launch {
            try {
                val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return@launch
                val categories = CategoryRepository.getInstance().getCategoriesForUser(userId)
                if (categories.isNotEmpty()) {
                    val defaultCategory = categories[0]
                    binding.ivCategoryIcon.setImageResource(defaultCategory.icon.resId)
                    binding.tvCategoryName.text = defaultCategory.title
                    binding.categoryIconBackground.setCardBackgroundColor(
                        requireContext().getColor(defaultCategory.color.colorResId)
                    )
                    viewModel.updateCategory(defaultCategory.title)
                    viewModel.updateCategoryId(defaultCategory.id)
                } else {
                    // No categories available, show error or default
                    showError("No categories available. Please create a category first.")
                }
            } catch (e: Exception) {
                println("Error loading default category: ${e.message}")
                e.printStackTrace()
                showError("Failed to load categories: ${e.message}")
            }
        }

        // Set default values
        viewModel.updateQuantity(30)
        viewModel.updateMeasurement("Mins")
        viewModel.updateFrequency(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
        viewModel.updateTime("5:00 - 12:00")
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Category selector
        binding.btnCategorySelector.setOnClickListener {
            showCategorySelector()
        }
        
        // Quantity selector
        binding.btnQuantitySelector.setOnClickListener {
            showQuantitySelector()
        }
        
        // Measurement selector
        binding.btnMeasurementSelector.setOnClickListener {
            showMeasurementSelector()
        }
        
        // Frequency selector
        binding.btnFrequencySelector.setOnClickListener {
            showFrequencySelector()
        }
        
        // Time selector
        binding.btnTimeSelector.setOnClickListener {
            showTimeSelector()
        }
        
        // Create button
        binding.btnCreate.setOnClickListener {
            validateAndCreateHabit()
        }
    }

    private fun showCategorySelector() {
        // Navigate to CategoryFragment to select a category
        findNavController().navigate(R.id.action_create_habit_to_category)
    }

    private fun showQuantitySelector() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_number_picker, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker).apply {
            minValue = 1
            maxValue = 999
            value = viewModel.quantity.value
            wrapSelectorWheel = false
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Quantity")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val quantity = numberPicker.value
                binding.tvQuantity.text = quantity.toString()
                viewModel.updateQuantity(quantity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMeasurementSelector() {
        val currentIndex = measurements.indexOf(viewModel.measurement.value)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Measurement")
            .setSingleChoiceItems(measurements.toTypedArray(), currentIndex) { dialog, which ->
                val selectedMeasurement = measurements[which]
                binding.tvMeasurement.text = selectedMeasurement
                viewModel.updateMeasurement(selectedMeasurement)
                dialog.dismiss()
            }
            .show()
    }

    private fun showFrequencySelector() {
        val checkedItems = BooleanArray(frequencies.size) { index ->
            viewModel.frequency.value.contains(frequencies[index])
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Select Frequency (Days of the Week)")
            .setMultiChoiceItems(frequencies.toTypedArray(), checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                val selectedDays = frequencies.filterIndexed { index, _ -> checkedItems[index] }
                if (selectedDays.isNotEmpty()) {
                    binding.tvFrequency.text = selectedDays.formatFrequency()
                    viewModel.updateFrequency(selectedDays)
                } else {
                    showError("Please select at least one day")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTimeSelector() {
        val calendar = Calendar.getInstance()
        
        // Show start time picker
        TimePickerDialog(
            requireContext(),
            { _, startHour, startMinute ->
                // Show end time picker after start time is selected
                TimePickerDialog(
                    requireContext(),
                    { _, endHour, endMinute ->
                        val timeRange = String.format(
                            "%02d:%02d - %02d:%02d",
                            startHour, startMinute, endHour, endMinute
                        )
                        binding.tvTime.text = timeRange
                        viewModel.updateTime(timeRange)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateAndCreateHabit() {
        val habitTitle = binding.etHabitTitle.text.toString().trim()
        
        // Validate habit title
        if (habitTitle.isEmpty()) {
            showError("Please enter a habit title")
            binding.etHabitTitle.requestFocus()
            return
        }
        
        if (habitTitle.length < 3) {
            showError("Habit title must be at least 3 characters")
            binding.etHabitTitle.requestFocus()
            return
        }

        // Validate category is selected
        if (viewModel.categoryId.value.isBlank()) {
            showError("Please select a category")
            return
        }

        // Update title in ViewModel
        viewModel.updateTitle(habitTitle)
        
        // Create the habit
        viewModel.createHabit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
