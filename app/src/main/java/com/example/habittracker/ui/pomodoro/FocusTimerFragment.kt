package com.example.habittracker.ui.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentFocusTimerBinding
import kotlinx.coroutines.launch

/**
 * FocusTimerFragment - Focus Timer Screen
 * 
 * Responsibilities:
 * - Display timer UI
 * - Handle user interactions
 * - Observe ViewModel state changes
 */
class FocusTimerFragment : Fragment() {

    private var _binding: FragmentFocusTimerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FocusTimerViewModel by viewModels()

    // Session dots views for dynamic updates
    private val sessionDots = mutableListOf<View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSessionDots()
        setupClickListeners()
        observeViewModel()
        
        // Get task name from arguments if provided
        arguments?.getString("taskName")?.let { taskName ->
            viewModel.setTaskName(taskName)
        }
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
        // Task selector - opens task picker
        binding.focusTaskContainer.setOnClickListener {
            onTaskSelectorClicked()
        }

        // Mode selectors
        binding.btnFocusMode.setOnClickListener {
            viewModel.setMode(FocusTimerViewModel.TimerMode.FOCUS)
        }

        binding.btnBreakMode.setOnClickListener {
            viewModel.setMode(FocusTimerViewModel.TimerMode.BREAK)
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
     * Handle task selector click
     */
    private fun onTaskSelectorClicked() {
        // Navigate to task picker or show task selection dialog
        // TODO: Implement task selection functionality
    }

    /**
     * Observe ViewModel state and update UI
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
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
        }

        // Update mode selector appearance
        updateModeSelector(state.mode)

        // Update play/pause button icon
        updatePlayPauseButton(state.status)

        // Update session progress dots
        updateSessionDots(state.completedSessions, state.totalSessions)
    }

    /**
     * Update mode selector visual state
     */
    private fun updateModeSelector(mode: FocusTimerViewModel.TimerMode) {
        when (mode) {
            FocusTimerViewModel.TimerMode.FOCUS -> {
                binding.btnFocusMode.setBackgroundResource(R.drawable.bg_mode_selected)
                binding.btnFocusMode.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                binding.btnBreakMode.setBackgroundResource(R.drawable.bg_mode_unselected)
                binding.btnBreakMode.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
            }
            FocusTimerViewModel.TimerMode.BREAK -> {
                binding.btnBreakMode.setBackgroundResource(R.drawable.bg_mode_selected)
                binding.btnBreakMode.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                binding.btnFocusMode.setBackgroundResource(R.drawable.bg_mode_unselected)
                binding.btnFocusMode.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
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
            // Add additional dots programmatically if needed
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
            val dot = View(requireContext()).apply {
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

    override fun onDestroyView() {
        super.onDestroyView()
        sessionDots.clear()
        _binding = null
    }
}
