package com.example.habittracker.ui.pomodoro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.habittracker.R
import com.example.habittracker.databinding.ActivityFocusTimerBinding
import com.example.habittracker.ui.main.MainActivity
import kotlinx.coroutines.launch

/**
 * FocusTimerActivity - Focus Timer Screen for Habits
 *
 * Responsibilities:
 * - Display timer UI for habit completion
 * - Handle user interactions
 * - Observe ViewModel state changes
 * - Complete habit when all sessions are done
 */
class FocusTimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusTimerBinding
    private val viewModel: FocusTimerViewModel by viewModels()

    // Session dots views for dynamic updates
    private val sessionDots = mutableListOf<View>()

    companion object {
        private const val EXTRA_HABIT_ID = "extra_habit_id"
        private const val EXTRA_HABIT_NAME = "extra_habit_name"
        private const val EXTRA_FOCUS_DURATION = "extra_focus_duration"
        private const val EXTRA_SHORT_BREAK = "extra_short_break"
        private const val EXTRA_LONG_BREAK = "extra_long_break"
        private const val EXTRA_TOTAL_SESSIONS = "extra_total_sessions"

        fun newIntent(
            context: Context,
            habitId: String,
            habitName: String,
            focusDuration: Int,
            shortBreak: Int,
            longBreak: Int,
            totalSessions: Int
        ): Intent {
            return Intent(context, FocusTimerActivity::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_HABIT_NAME, habitName)
                putExtra(EXTRA_FOCUS_DURATION, focusDuration)
                putExtra(EXTRA_SHORT_BREAK, shortBreak)
                putExtra(EXTRA_LONG_BREAK, longBreak)
                putExtra(EXTRA_TOTAL_SESSIONS, totalSessions)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityFocusTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MainActivity.hideSystemUI(this)
        applyWindowInsets()
        setupSessionDots()
        setupClickListeners()
        observeViewModel()

        // Configure timer with habit settings
        configureTimerFromIntent()
    }

    /**
     * Apply window insets to handle status bar properly
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply padding top for status bar
            view.updatePadding(
                top = systemBarsInsets.top
            )

            // Pass insets down to child views
            windowInsets
        }

        // Handle bottom inset (navigation/gesture bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.timerCard) { view, windowInsets ->
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

    /**
     * Configure timer settings from intent extras
     */
    private fun configureTimerFromIntent() {
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Habit"
        val focusDuration = intent.getIntExtra(EXTRA_FOCUS_DURATION, 25)
        val shortBreak = intent.getIntExtra(EXTRA_SHORT_BREAK, 5)
        val longBreak = intent.getIntExtra(EXTRA_LONG_BREAK, 15)
        val totalSessions = intent.getIntExtra(EXTRA_TOTAL_SESSIONS, 4)

        // Set task name
        viewModel.setTaskName(habitName)

        // Configure timer durations
        viewModel.setFocusDuration(focusDuration)
        viewModel.setBreakDuration(shortBreak)
        viewModel.setLongBreakDuration(longBreak)
        viewModel.setTotalSessions(totalSessions)

        // Start in focus mode
        viewModel.setMode(FocusTimerViewModel.TimerMode.FOCUS)
    }

    /**
     * Initialize session dots references
     */
    private fun setupSessionDots() {
        sessionDots.clear()
        sessionDots.add(binding.dot1)
        sessionDots.add(binding.dot2)
        sessionDots.add(binding.dot3)
        sessionDots.add(binding.dot4)
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }


        // Timer controls
        binding.btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.btnStop.setOnClickListener {
            viewModel.stopTimer()
        }
    }

    /**
     * Observe ViewModel state and update UI
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    /**
     * Update UI based on state
     */
    private fun updateUI(state: FocusTimerViewModel.FocusTimerUiState) {
        // Update task name
        binding.tvTaskName.text = state.taskName

        // Update timer display
        binding.tvTimerDisplay.text = viewModel.formatTime(state.remainingTimeMillis)

        // Update progress indicator
        binding.circularProgress.progress = state.progress.toInt()

        // Update timer type label
        binding.tvTimerTypeLabel.text = when (state.mode) {
            FocusTimerViewModel.TimerMode.FOCUS -> getString(R.string.focus_time)
            FocusTimerViewModel.TimerMode.BREAK -> getString(R.string.break_time)
            FocusTimerViewModel.TimerMode.LONG_BREAK -> "Long Break"
        }

        // Update mode selector appearance
        updateModeSelector(state.mode)

        // Update play/pause button icon
        updatePlayPauseButton(state.status)

        // Update session progress dots
        updateSessionDots(state.completedSessions, state.totalSessions)

        // Check if all sessions completed
        if (state.completedSessions >= state.totalSessions && state.mode == FocusTimerViewModel.TimerMode.FOCUS) {
            onAllSessionsCompleted()
        }
    }

    /**
     * Handle completion of all pomodoro sessions
     */
    private fun onAllSessionsCompleted() {
        // Complete the habit by returning result to ViewHabitDetailActivity
        val resultIntent = Intent().apply {
            putExtra("habit_completed", true)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    /**
     * Update mode selector visual state
     */
    private fun updateModeSelector(mode: FocusTimerViewModel.TimerMode) {
        when (mode) {
            FocusTimerViewModel.TimerMode.FOCUS -> {
                binding.btnFocusMode.setBackgroundResource(R.drawable.bg_mode_selected)
                binding.btnFocusMode.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                binding.btnBreakMode.setBackgroundResource(R.drawable.bg_mode_unselected)
                binding.btnBreakMode.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            }
            FocusTimerViewModel.TimerMode.BREAK, FocusTimerViewModel.TimerMode.LONG_BREAK -> {
                binding.btnBreakMode.setBackgroundResource(R.drawable.bg_mode_selected)
                binding.btnBreakMode.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                binding.btnFocusMode.setBackgroundResource(R.drawable.bg_mode_unselected)
                binding.btnFocusMode.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            }
        }
    }

    /**
     * Update play/pause button based on timer status
     */
    private fun updatePlayPauseButton(status: FocusTimerViewModel.TimerStatus) {
        when (status) {
            FocusTimerViewModel.TimerStatus.IDLE,
            FocusTimerViewModel.TimerStatus.PAUSED -> {
                binding.ivPlayPause.setImageResource(R.drawable.ic_play)
            }
            FocusTimerViewModel.TimerStatus.RUNNING -> {
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    /**
     * Update session progress dots
     */
    private fun updateSessionDots(completedSessions: Int, totalSessions: Int) {
        // Update existing dots
        sessionDots.forEachIndexed { index, dot ->
            if (index < totalSessions) {
                dot.visibility = View.VISIBLE
                if (index < completedSessions) {
                    dot.setBackgroundResource(R.drawable.bg_session_dot_active)
                } else {
                    dot.setBackgroundResource(R.drawable.bg_session_dot_inactive)
                }
            } else {
                dot.visibility = View.GONE
            }
        }

        // If more than 4 sessions, dynamically add dots
        if (totalSessions > 4) {
            addExtraSessionDots(totalSessions)
        }
    }

    /**
     * Add extra session dots for more than 4 sessions
     */
    private fun addExtraSessionDots(totalSessions: Int) {
        val container = binding.sessionDotsContainer
        val currentDotCount = sessionDots.size

        for (i in currentDotCount until totalSessions) {
            val dot = View(this).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    resources.getDimensionPixelSize(R.dimen.session_dot_size),
                    resources.getDimensionPixelSize(R.dimen.session_dot_size)
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.session_dot_margin)
                }
                setBackgroundResource(R.drawable.bg_session_dot_inactive)
            }
            container.addView(dot)
            sessionDots.add(dot)
        }
    }
}
