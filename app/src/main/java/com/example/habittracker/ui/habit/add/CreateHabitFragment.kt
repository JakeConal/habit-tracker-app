package com.example.habittracker.ui.habit.add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.databinding.FragmentCreateHabitBinding
import com.example.habittracker.ui.common.BaseFragment
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.util.formatFrequency
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * CreateHabitFragment - Screen for creating a new habit
 * Follows MVVM architecture pattern
 */
class CreateHabitFragment : BaseFragment<FragmentCreateHabitBinding>() {

    private val viewModel: CreateHabitViewModel by viewModels()
    
    // Flag to prevent setupInitialValues from overwriting user-selected category
    private var isInitialCategoryLoaded = false
    
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
        applyWindowInsets()
        setupInitialValues()
    }

    private fun applyWindowInsets() {
        // Xử lý edge-to-edge và window insets
        
        // 1. Root container xử lý top inset (status bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Áp padding top cho status bar
            view.updatePadding(
                top = systemBarsInsets.top
            )
            
            // Truyền insets xuống các child views
            windowInsets
        }
        
        // 2. Content container xử lý bottom inset (navigation/gesture bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentContainer) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navBarHeight = systemBarsInsets.bottom
            
            // Áp padding bottom = spacing_md (16dp) + chiều cao nav bar
            val basePadding = resources.getDimensionPixelSize(R.dimen.spacing_md)
            view.updatePadding(
                bottom = basePadding + navBarHeight
            )
            
            // Consume bottom inset để không ảnh hưởng views khác
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun observeData() {
        // Observe category changes and update UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.category.collect { category ->
                category?.let {
                    updateCategoryUI(it)
                }
            }
        }

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

        // Listen for category selection result from CategoryActivity
        parentFragmentManager.setFragmentResultListener(
            "category_request_key",
            viewLifecycleOwner
        ) { _, bundle ->
            val categoryId = bundle.getString("selected_category_id")
            
            // Only use categoryId - load full Category from repository
            if (categoryId != null) {
                isInitialCategoryLoaded = true // Mark as loaded to prevent default from overwriting
                viewModel.loadCategory(categoryId)
            }
        }
    }

    /**
     * Update category UI from Category object
     * This is the single source of truth for category display
     */
    private fun updateCategoryUI(category: Category) {
        binding.tvCategoryName.text = category.title
        binding.ivCategoryIcon.setImageResource(category.icon.resId)
        binding.categoryIconBackground.setBackgroundResource(category.color.resId)
    }

    private fun setupInitialValues() {
        // Load default category from repository only if not already loaded
        // This prevents overwriting user-selected category from CategoryActivity
        if (!isInitialCategoryLoaded) {
            lifecycleScope.launch {
                try {
                    val userId = AuthRepository.getInstance().getCurrentUser()?.uid ?: return@launch
                    val categories = CategoryRepository.getInstance().getCategoriesForUser(userId)
                    if (categories.isNotEmpty()) {
                        val defaultCategory = categories[0]
                        // Load category through ViewModel to ensure consistency
                        viewModel.loadCategory(defaultCategory.id)
                        isInitialCategoryLoaded = true
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
        // Launch CategoryActivity to select a category
        val intent = Intent(requireContext(), com.example.habittracker.ui.category.CategoryActivity::class.java)
        startActivity(intent)
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
