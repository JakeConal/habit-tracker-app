package com.example.habittracker.ui.habit.add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentCreateHabitBinding
import com.example.habittracker.ui.common.BaseFragment
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * CreateHabitFragment - Screen for creating a new habit
 * Follows MVVM architecture pattern
 */
class CreateHabitFragment : BaseFragment<FragmentCreateHabitBinding>() {

    private val viewModel: CreateHabitViewModel by viewModels()
    
    // Categories with their corresponding icons
    private val categories = listOf(
        "Reading" to R.drawable.ic_book,
        "Exercise" to R.drawable.ic_fitness,
        "Study" to R.drawable.ic_book,
        "Work" to R.drawable.ic_work,
        "Health" to R.drawable.ic_health,
        "Meditation" to R.drawable.ic_meditation,
        "Other" to R.drawable.ic_other
    )
    
    private val measurements = listOf("Mins", "Hours", "Pages", "Times", "Km", "Miles")
    private val frequencies = listOf("Everyday", "Weekly", "Monthly", "Custom")

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateHabitBinding {
        return FragmentCreateHabitBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupClickListeners()
        setupInitialValues()
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
        
        // Listen for category selection result from CategoryFragment
        parentFragmentManager.setFragmentResultListener(
            "category_request_key",
            viewLifecycleOwner
        ) { _, bundle ->
            val categoryName = bundle.getString("selected_category_name")
            val categoryIcon = bundle.getInt("selected_category_icon")
            
            if (categoryName != null && categoryIcon != 0) {
                binding.ivCategoryIcon.setImageResource(categoryIcon)
                binding.tvCategoryName.text = categoryName
                viewModel.updateCategory(categoryName)
            }
        }
    }

    private fun setupInitialValues() {
        // Set default category
        binding.ivCategoryIcon.setImageResource(categories[0].second)
        binding.tvCategoryName.text = categories[0].first
        viewModel.updateCategory(categories[0].first)
        
        // Set default values
        viewModel.updateQuantity(30)
        viewModel.updateMeasurement("Mins")
        viewModel.updateFrequency("Everyday")
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
        val currentIndex = frequencies.indexOf(viewModel.frequency.value)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Frequency")
            .setSingleChoiceItems(frequencies.toTypedArray(), currentIndex) { dialog, which ->
                val selectedFrequency = frequencies[which]
                binding.tvFrequency.text = selectedFrequency
                viewModel.updateFrequency(selectedFrequency)
                dialog.dismiss()
            }
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
        
        // Update title in ViewModel
        viewModel.updateTitle(habitTitle)
        
        // Create the habit
        viewModel.createHabit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
