package com.example.habittracker.ui.habit.add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.R
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.databinding.ActivityCreateHabitBinding
import com.example.habittracker.ui.category.CategoryActivity
import com.example.habittracker.ui.main.MainActivity
import com.example.habittracker.util.formatFrequency
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * CreateHabitActivity - Activity for creating a new habit
 * Follows MVVM architecture pattern
 */
class CreateHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateHabitBinding
    private val viewModel: CreateHabitViewModel by viewModels()
    
    // Flag to prevent setupInitialValues from overwriting user-selected category
    private var isInitialCategoryLoaded = false
    
    private val measurements = listOf(
        "Mins", "Hours", "Pages", "Times", "Km", "Miles",
        "Steps", "Glasses", "Cups", "Calories", "Litres", "Ml",
        "Words", "Lessons", "Exercises", "Kg", "Lbs", "Minutes", "Hours"
    ).distinct()
    private val frequencies = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    companion object {
        private const val REQUEST_CODE_CATEGORY = 1001
        const val EXTRA_CATEGORY_ID = "extra_category_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        MainActivity.hideSystemUI(this)
        applyWindowInsets()
        setupClickListeners()
        observeData()
        setupInitialValues()
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
        // Observe category changes and update UI
        lifecycleScope.launch {
            viewModel.category.collect { category ->
                category?.let {
                    updateCategoryUI(it)
                }
            }
        }

        // Observe habit creation success
        lifecycleScope.launch {
            viewModel.habitCreated.collect { success ->
                if (success) {
                    showSuccess("Habit created successfully!")
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

        // Observe frequency changes
        lifecycleScope.launch {
            viewModel.frequency.collect { frequency ->
                binding.tvFrequency.text = frequency.formatFrequency()
            }
        }

        // Observe time changes
        lifecycleScope.launch {
            viewModel.time.collect { time ->
                binding.tvTime.text = time
            }
        }

        // Observe quantity changes
        lifecycleScope.launch {
            viewModel.quantity.collect { quantity ->
                if (binding.etQuantity.text.toString() != quantity.toString()) {
                    binding.etQuantity.setText(quantity.toString())
                }
            }
        }

        // Observe measurement changes
        lifecycleScope.launch {
            viewModel.measurement.collect { measurement ->
                binding.tvMeasurement.text = measurement
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
        if (!isInitialCategoryLoaded) {
            lifecycleScope.launch {
                try {
                    val userId = AuthRepository.getInstance().getCurrentUser()?.uid
                    if (userId != null) {
                        val categories = CategoryRepository.getInstance().getCategoriesForUser(userId)
                        // Use first category as default if available
                        if (categories.isNotEmpty()) {
                            viewModel.loadCategory(categories[0].id)
                        }
                    }
                    isInitialCategoryLoaded = true
                } catch (e: Exception) {
                    showError("Failed to load categories: ${e.message}")
                }
            }
        }

        // Set default values
        viewModel.updateMeasurement("Mins")
        viewModel.updateFrequency(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
        viewModel.updateTime("5:00 - 12:00")
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
        
        // Quantity is now an EditText, we don't need btnQuantitySelector listener
        // But we need to update ViewModel when EditText changes
        binding.etQuantity.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val quantity = s.toString().toIntOrNull() ?: 0
                viewModel.updateQuantity(quantity)
            }
        })

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
        // Start CategoryActivity for result
        val intent = Intent(this, CategoryActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CATEGORY)
    }

    /**
     * Show measurement selector dialog
     */
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
            this,
            { _, startHour, startMinute ->
                // Show end time picker after start time is selected
                TimePickerDialog(
                    this,
                    { _, endHour, endMinute ->
                        val timeRange = java.util.Locale.getDefault().let { locale ->
                            String.format(
                                locale,
                                "%02d:%02d - %02d:%02d",
                                startHour, startMinute, endHour, endMinute
                            )
                        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CATEGORY && resultCode == RESULT_OK) {
            val categoryId = data?.getStringExtra(EXTRA_CATEGORY_ID)
            if (categoryId != null) {
                isInitialCategoryLoaded = true
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
