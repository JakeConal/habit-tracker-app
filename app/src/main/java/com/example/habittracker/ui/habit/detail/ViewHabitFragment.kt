package com.example.habittracker.ui.habit.detail

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentViewHabitBinding
import com.example.habittracker.ui.common.BaseFragment
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewHabitFragment - Screen for viewing and editing an existing habit
 * Follows MVVM architecture pattern
 */
class ViewHabitFragment : BaseFragment<FragmentViewHabitBinding>() {

    private val viewModel: ViewHabitViewModel by viewModels()
    
    private val habitId: Long by lazy {
        arguments?.getLong("habitId") ?: 0L
    }

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
    ): FragmentViewHabitBinding {
        return FragmentViewHabitBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupClickListeners()
        // Load habit data
        viewModel.loadHabit(habitId)
    }

    override fun observeData() {
        // Observe habit data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habit.collect { habit ->
                habit?.let {
                    binding.tvTitle.text = it.name
                    binding.etHabitTitle.setText(it.name)
                    
                    // Set category icon if available
                    if (it.iconRes != 0) {
                        binding.ivCategoryIcon.setImageResource(it.iconRes)
                    }
                    
                    // Set category icon background if available
                    if (it.iconBackgroundRes != 0) {
                        updateCategoryIconBackground(it.iconBackgroundRes)
                    }
                }
            }
        }

        // Observe quantity
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.quantity.collect { quantity ->
                binding.tvQuantity.text = quantity.toString()
            }
        }

        // Observe measurement
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.measurement.collect { measurement ->
                binding.tvMeasurement.text = measurement
            }
        }

        // Observe frequency
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.frequency.collect { frequency ->
                binding.tvFrequency.text = frequency
            }
        }

        // Observe time
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.time.collect { time ->
                binding.tvTime.text = time
            }
        }

        // Observe habit update success
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitUpdated.collect { success ->
                if (success) {
                    showSuccess(getString(R.string.habit_updated))
                    findNavController().navigateUp()
                }
            }
        }

        // Observe habit delete success
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitDeleted.collect { success ->
                if (success) {
                    showSuccess(getString(R.string.habit_deleted))
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
            val categoryIconBackground = bundle.getInt("selected_category_icon_background")

            if (categoryName != null && categoryIcon != 0) {
                binding.ivCategoryIcon.setImageResource(categoryIcon)
                binding.tvCategoryName.text = categoryName
                updateCategoryIconBackground(categoryIconBackground)
                viewModel.updateCategory(categoryName)
                viewModel.updateCategoryIcon(categoryIcon)
                viewModel.updateCategoryIconBackground(categoryIconBackground)
            }
        }
    }

    private fun updateCategoryIconBackground(backgroundRes: Int) {
        val colorRes = when (backgroundRes) {
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
        binding.categoryIconBackground.setCardBackgroundColor(
            requireContext().getColor(colorRes)
        )
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

        // Start Pomodoro button
        binding.btnStartPomodoro.setOnClickListener {
            navigateToFocusTimer()
        }

        // Delete button
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            validateAndSaveHabit()
        }
    }

    private fun showCategorySelector() {
        // Navigate to CategoryFragment to select a category
        findNavController().navigate(R.id.action_view_habit_to_category)
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

    private fun navigateToFocusTimer() {
        val habitName = binding.etHabitTitle.text.toString().ifEmpty { 
            viewModel.title.value 
        }
        val bundle = bundleOf("taskName" to habitName)
        findNavController().navigate(R.id.action_view_habit_to_focus_timer, bundle)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteHabit()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun validateAndSaveHabit() {
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

        // Save the habit
        viewModel.saveHabit()
    }
}
