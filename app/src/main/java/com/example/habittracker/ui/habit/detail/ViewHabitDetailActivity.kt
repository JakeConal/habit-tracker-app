package com.example.habittracker.ui.habit.detail

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityViewHabitDetailBinding
import com.example.habittracker.ui.category.CategoryActivity
import com.example.habittracker.ui.habit.add.CreateHabitActivity
import com.example.habittracker.ui.main.MainActivity
import com.example.habittracker.ui.pomodoro.FocusTimerActivity
import com.example.habittracker.util.DateUtils
import com.example.habittracker.util.formatFrequency
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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

    private val measurements = listOf(
        "Mins", "Hours", "Pages", "Times", "Km", "Miles",
        "Steps", "Glasses", "Cups", "Calories", "Litres", "Ml",
        "Words", "Lessons", "Exercises", "Kg", "Lbs", "Minutes", "Hours"
    ).distinct()
    private val frequencies = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        private const val REQUEST_CODE_CATEGORY = 1001
        private const val REQUEST_CODE_FOCUS_TIMER = 1002

        fun newIntent(context: Context, habitId: String): Intent {
            return Intent(context, ViewHabitDetailActivity::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habitId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityViewHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        MainActivity.hideSystemUI(this)
        applyWindowInsets()
        setupClickListeners()
        observeData()
        
        // Load habit data only if not already loaded
        if (viewModel.habit.value == null) {
            viewModel.loadHabit(habitId)
        }
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
        // Observe habit changes
        lifecycleScope.launch {
            viewModel.habit.collect { habit ->
                habit?.let {
                    binding.tvTitle.text = it.name
                    binding.etHabitTitle.setText(it.name)

                    if (it.isChallengeHabit) {
                        binding.challengeInfoCard.visibility = android.view.View.VISIBLE
                        binding.layoutHabitTitle.visibility = android.view.View.GONE
                        binding.layoutCategory.visibility = android.view.View.GONE
                        binding.layoutQuantityMeasurement.visibility = android.view.View.GONE
                        binding.layoutFrequency.visibility = android.view.View.GONE
                        binding.btnSaveHeader.visibility = android.view.View.GONE

                        binding.tvChallengeDescription.text = it.challengeDescription

                        if (!it.challengeImageUrl.isNullOrEmpty()) {
                            Glide.with(this@ViewHabitDetailActivity)
                                .load(it.challengeImageUrl)
                                .placeholder(R.drawable.placeholder_challenge)
                                .into(binding.ivChallengeImage)
                        }
                    } else {
                        binding.challengeInfoCard.visibility = android.view.View.GONE
                        binding.layoutHabitTitle.visibility = android.view.View.VISIBLE
                        binding.layoutCategory.visibility = android.view.View.VISIBLE
                        binding.layoutQuantityMeasurement.visibility = android.view.View.VISIBLE
                        binding.layoutFrequency.visibility = android.view.View.VISIBLE
                        binding.btnSaveHeader.visibility = android.view.View.VISIBLE
                    }

                    updateActionButton(it.completedDates.contains(DateUtils.getCurrentDateString()), it.isPomodoroRequired)
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

        // Observe quantity changes
        lifecycleScope.launch {
            viewModel.quantity.collect { quantity ->
                if (binding.etQuantity.text.toString() != quantity.toString()) {
                    binding.etQuantity.setText(quantity.toString())
                }
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

        // Observe Pomodoro settings
        lifecycleScope.launch {
            viewModel.isPomodoroRequired.collect { isRequired ->
                binding.switchPomodoro.isChecked = isRequired
                binding.layoutPomodoroSettings.visibility = if (isRequired) android.view.View.VISIBLE else android.view.View.GONE

                val isTodayCompleted = viewModel.habit.value?.completedDates?.contains(DateUtils.getCurrentDateString()) == true
                updateActionButton(isTodayCompleted, isRequired)
            }
        }

        lifecycleScope.launch {
            viewModel.focusDuration.collect { duration ->
                if (binding.etFocusDuration.text.toString() != duration.toString()) {
                    binding.etFocusDuration.setText(duration.toString())
                }
            }
        }

        lifecycleScope.launch {
            viewModel.shortBreak.collect { duration ->
                if (binding.etShortBreak.text.toString() != duration.toString()) {
                    binding.etShortBreak.setText(duration.toString())
                }
            }
        }

        lifecycleScope.launch {
            viewModel.longBreak.collect { duration ->
                if (binding.etLongBreak.text.toString() != duration.toString()) {
                    binding.etLongBreak.setText(duration.toString())
                }
            }
        }

        lifecycleScope.launch {
            viewModel.totalSessions.collect { sessions ->
                if (binding.etTotalSessions.text.toString() != sessions.toString()) {
                    binding.etTotalSessions.setText(sessions.toString())
                }
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

    private fun updateActionButton(isCompleted: Boolean, isPomodoroRequired: Boolean) {
        if (isCompleted) {
            binding.btnActionButton.text = getString(R.string.habit_completed)
            binding.btnActionButton.isEnabled = false
            // Use alpha to visually indicate disabled state
            binding.btnActionButton.alpha = 0.6f
        } else {
            binding.btnActionButton.isEnabled = true
            binding.btnActionButton.alpha = 1.0f
            binding.btnActionButton.text = if (isPomodoroRequired) {
                getString(R.string.start_pomodoro)
            } else {
                getString(R.string.complete_habit)
            }
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Save button in header
        binding.btnSaveHeader.setOnClickListener {
            validateAndSaveHabit()
        }

        // Category selector
        binding.btnCategorySelector.setOnClickListener {
            showCategorySelector()
        }

        // Quantity is now an EditText
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

        // Pomodoro switch
        binding.switchPomodoro.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updatePomodoroRequired(isChecked)
        }

        // Pomodoro durations
        binding.etFocusDuration.addTextChangedListener(createSimpleTextWatcher { text ->
            viewModel.updateFocusDuration(text.toIntOrNull() ?: 25)
        })

        binding.etShortBreak.addTextChangedListener(createSimpleTextWatcher { text ->
            viewModel.updateShortBreak(text.toIntOrNull() ?: 5)
        })

        binding.etLongBreak.addTextChangedListener(createSimpleTextWatcher { text ->
            viewModel.updateLongBreak(text.toIntOrNull() ?: 15)
        })

        binding.etTotalSessions.addTextChangedListener(createSimpleTextWatcher { text ->
            viewModel.updateTotalSessions(text.toIntOrNull() ?: 4)
        })

        // Action button (Start Pomodoro or Complete Habit)
        binding.btnActionButton.setOnClickListener {
            if (viewModel.isPomodoroRequired.value) {
                navigateToFocusTimer()
            } else {
                viewModel.completeHabit()
            }
        }
    }

    private fun showCategorySelector() {
        // Start CategoryActivity for result
        val intent = Intent(this, CategoryActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CATEGORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CATEGORY && resultCode == RESULT_OK) {
            val categoryId = data?.getStringExtra(CreateHabitActivity.EXTRA_CATEGORY_ID)
            categoryId?.let {
                viewModel.updateCategoryId(it)
                viewModel.loadCategory(it)
            }
        } else if (requestCode == REQUEST_CODE_FOCUS_TIMER && resultCode == RESULT_OK) {
            val habitCompleted = data?.getBooleanExtra("habit_completed", false) ?: false
            if (habitCompleted) {
                viewModel.completeHabit()
            }
        }
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

    private fun createSimpleTextWatcher(onChanged: (String) -> Unit) = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            onChanged(s.toString())
        }
    }


    private fun navigateToFocusTimer() {
        val habitName = binding.etHabitTitle.text.toString().ifEmpty { 
            viewModel.title.value 
        }
        val focusDuration = viewModel.focusDuration.value
        val shortBreak = viewModel.shortBreak.value
        val longBreak = viewModel.longBreak.value
        val totalSessions = viewModel.totalSessions.value

        val intent = FocusTimerActivity.newIntent(
            this,
            habitId,
            habitName,
            focusDuration,
            shortBreak,
            longBreak,
            totalSessions
        )
        startActivityForResult(intent, REQUEST_CODE_FOCUS_TIMER)
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

        // Update the habit title in ViewModel before saving
        viewModel.updateTitle(habitTitle)

        // Quantity was updated via text listener

        viewModel.saveHabit()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
