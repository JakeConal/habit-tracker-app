package com.example.habittracker.ui.habit.detail

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityViewHabitDetailBinding
import com.example.habittracker.ui.category.CategoryActivity
import com.example.habittracker.util.formatFrequency
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewHabitDetailActivity - Activity for viewing and editing an existing habit
 * Follows MVVM architecture pattern
 */
class ViewHabitDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewHabitDetailBinding
    private val viewModel: ViewHabitViewModel by viewModels()
    
    private val habitId: String by lazy {
        intent.getStringExtra(EXTRA_HABIT_ID) ?: ""
    }

    private val measurements = listOf("Mins", "Hours", "Pages", "Times", "Km", "Miles")
    private val frequencies = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        private const val REQUEST_CODE_CATEGORY = 1001
        
        fun newIntent(context: Context, habitId: String): Intent {
            return Intent(context, ViewHabitDetailActivity::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habitId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupView()
        applyWindowInsets()
        setupClickListeners()
        observeData()
        
        // Load habit data only if not already loaded
        if (viewModel.habit.value == null) {
            viewModel.loadHabit(habitId)
        }
    }

    private fun setupView() {
        // Enable edge-to-edge
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun applyWindowInsets() {
        // Handle edge-to-edge and window insets
        
        // 1. Root container handles top inset (status bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding top for status bar
            view.updatePadding(
                top = systemBarsInsets.top
            )
            
            // Pass insets down to child views
            windowInsets
        }
        
        // 2. Content container handles bottom inset (navigation/gesture bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentContainer) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navBarHeight = systemBarsInsets.bottom
            
            // Apply padding bottom = spacing_md (16dp) + nav bar height
            val basePadding = resources.getDimensionPixelSize(R.dimen.spacing_md)
            view.updatePadding(
                bottom = basePadding + navBarHeight
            )
            
            // Consume bottom inset
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observeData() {
        // Observe habit data
        lifecycleScope.launch {
            viewModel.habit.collect { habit ->
                habit?.let {
                    binding.tvTitle.text = it.name
                    binding.etHabitTitle.setText(it.name)
                }
            }
        }

        // Observe category
        lifecycleScope.launch {
            viewModel.category.collect { category ->
                category?.let {
                    binding.ivCategoryIcon.setImageResource(it.icon.resId)
                    binding.tvCategoryName.text = it.title
                    updateCategoryIconBackground(it.color.resId)
                }
            }
        }

        // Observe quantity
        lifecycleScope.launch {
            viewModel.quantity.collect { quantity ->
                binding.tvQuantity.text = quantity.toString()
            }
        }

        // Observe measurement
        lifecycleScope.launch {
            viewModel.measurement.collect { measurement ->
                binding.tvMeasurement.text = measurement
            }
        }

        // Observe frequency
        lifecycleScope.launch {
            viewModel.frequency.collect { frequency ->
                binding.tvFrequency.text = frequency.formatFrequency()
            }
        }

        // Observe time
        lifecycleScope.launch {
            viewModel.time.collect { time ->
                binding.tvTime.text = time
            }
        }

        // Observe habit update success
        lifecycleScope.launch {
            viewModel.habitUpdated.collect { success ->
                if (success) {
                    showSuccess(getString(R.string.habit_updated))
                    finish()
                }
            }
        }

        // Observe habit delete success
        lifecycleScope.launch {
            viewModel.habitDeleted.collect { success ->
                if (success) {
                    showSuccess(getString(R.string.habit_deleted))
                    finish()
                }
            }
        }

        // Observe errors
        lifecycleScope.launch {
            viewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    showError(it)
                }
            }
        }
    }

    private fun updateCategoryIconBackground(backgroundRes: Int) {
        binding.categoryIconBackground.setBackgroundResource(backgroundRes)
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
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
        // Start CategoryActivity for result
        val intent = Intent(this, CategoryActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CATEGORY)
    }

    private fun showQuantitySelector() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_number_picker, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker).apply {
            minValue = 1
            maxValue = 999
            value = viewModel.quantity.value
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
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

        AlertDialog.Builder(this)
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

        AlertDialog.Builder(this)
            .setTitle("Select Frequency (Days of the Week)")
            .setMultiChoiceItems(frequencies.toTypedArray(), checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                val selectedDays = frequencies.filterIndexed { index, _ -> checkedItems[index] }
                if (selectedDays.isNotEmpty()) {
                    binding.tvFrequency.text = selectedDays.joinToString(", ")
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
            this,
            { _, startHour, startMinute ->
                // Show end time picker after start time is selected
                TimePickerDialog(
                    this,
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
        // TODO: Navigate to FocusTimerActivity with habitName
        showError("Focus Timer not implemented yet")
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CATEGORY && resultCode == RESULT_OK) {
            val categoryId = data?.getStringExtra(com.example.habittracker.ui.habit.add.CreateHabitActivity.EXTRA_CATEGORY_ID)
            if (categoryId != null) {
                viewModel.updateCategoryId(categoryId)
                viewModel.loadCategory(categoryId)
            }
        }
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
