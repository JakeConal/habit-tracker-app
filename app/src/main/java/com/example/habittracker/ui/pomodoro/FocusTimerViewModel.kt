package com.example.habittracker.ui.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FocusTimerViewModel - Manages Focus Timer state and logic
 * 
 * Responsibilities:
 * - Timer state management (idle, running, paused)
 * - Mode management (Focus / Break)
 * - Session tracking
 */
class FocusTimerViewModel : ViewModel() {

    // Timer Status enum
    enum class TimerStatus {
        IDLE,
        RUNNING,
        PAUSED
    }

    // Timer Mode enum
    enum class TimerMode {
        FOCUS,
        BREAK
    }

    // UI State data class
    data class FocusTimerUiState(
        val taskName: String = "Walk",
        val mode: TimerMode = TimerMode.FOCUS,
        val status: TimerStatus = TimerStatus.IDLE,
        val totalTimeMillis: Long = 25 * 60 * 1000L, // 25 minutes
        val remainingTimeMillis: Long = 25 * 60 * 1000L,
        val progress: Float = 100f,
        val totalSessions: Int = 4,
        val currentSession: Int = 1,
        val completedSessions: Int = 0,
        val focusDurationMinutes: Int = 25,
        val breakDurationMinutes: Int = 5
    )

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    // Configuration
    private var focusDurationMinutes = 25
    private var breakDurationMinutes = 5
    private var totalSessions = 4

    /**
     * Set the task name being focused on
     */
    fun setTaskName(name: String) {
        _uiState.value = _uiState.value.copy(taskName = name)
    }

    /**
     * Switch between Focus and Break mode
     */
    fun setMode(mode: TimerMode) {
        if (_uiState.value.status == TimerStatus.RUNNING) {
            return // Don't allow mode change while timer is running
        }

        val newTotalTime = when (mode) {
            TimerMode.FOCUS -> focusDurationMinutes * 60 * 1000L
            TimerMode.BREAK -> breakDurationMinutes * 60 * 1000L
        }

        _uiState.value = _uiState.value.copy(
            mode = mode,
            totalTimeMillis = newTotalTime,
            remainingTimeMillis = newTotalTime,
            progress = 100f,
            status = TimerStatus.IDLE
        )
    }

    /**
     * Start or resume the timer
     */
    fun startTimer() {
        val currentState = _uiState.value

        when (currentState.status) {
            TimerStatus.IDLE, TimerStatus.PAUSED -> {
                _uiState.value = currentState.copy(status = TimerStatus.RUNNING)
                startCountDown(currentState.remainingTimeMillis)
            }
            TimerStatus.RUNNING -> {
                // Already running, do nothing
            }
        }
    }

    /**
     * Pause the timer
     */
    fun pauseTimer() {
        countDownTimer?.cancel()
        _uiState.value = _uiState.value.copy(status = TimerStatus.PAUSED)
    }

    /**
     * Stop and reset the timer
     */
    fun stopTimer() {
        countDownTimer?.cancel()
        
        val currentState = _uiState.value
        val totalTime = currentState.totalTimeMillis

        _uiState.value = currentState.copy(
            status = TimerStatus.IDLE,
            remainingTimeMillis = totalTime,
            progress = 100f
        )
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        when (_uiState.value.status) {
            TimerStatus.IDLE, TimerStatus.PAUSED -> startTimer()
            TimerStatus.RUNNING -> pauseTimer()
        }
    }

    /**
     * Set focus duration in minutes
     */
    fun setFocusDuration(minutes: Int) {
        focusDurationMinutes = minutes
        _uiState.value = _uiState.value.copy(focusDurationMinutes = minutes)
        
        if (_uiState.value.mode == TimerMode.FOCUS && _uiState.value.status == TimerStatus.IDLE) {
            val newTotalTime = minutes * 60 * 1000L
            _uiState.value = _uiState.value.copy(
                totalTimeMillis = newTotalTime,
                remainingTimeMillis = newTotalTime
            )
        }
    }

    /**
     * Set break duration in minutes
     */
    fun setBreakDuration(minutes: Int) {
        breakDurationMinutes = minutes
        _uiState.value = _uiState.value.copy(breakDurationMinutes = minutes)
        
        if (_uiState.value.mode == TimerMode.BREAK && _uiState.value.status == TimerStatus.IDLE) {
            val newTotalTime = minutes * 60 * 1000L
            _uiState.value = _uiState.value.copy(
                totalTimeMillis = newTotalTime,
                remainingTimeMillis = newTotalTime
            )
        }
    }

    /**
     * Set total number of sessions
     */
    fun setTotalSessions(sessions: Int) {
        totalSessions = sessions
        _uiState.value = _uiState.value.copy(totalSessions = sessions)
    }

    /**
     * Start countdown timer
     */
    private fun startCountDown(timeMillis: Long) {
        countDownTimer = object : CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val currentState = _uiState.value
                val progress = (millisUntilFinished.toFloat() / currentState.totalTimeMillis) * 100f

                _uiState.value = currentState.copy(
                    remainingTimeMillis = millisUntilFinished,
                    progress = progress
                )
            }

            override fun onFinish() {
                onTimerComplete()
            }
        }.start()
    }

    /**
     * Handle timer completion
     */
    private fun onTimerComplete() {
        val currentState = _uiState.value

        when (currentState.mode) {
            TimerMode.FOCUS -> {
                // Completed a focus session
                val newCompletedSessions = currentState.completedSessions + 1
                val newCurrentSession = if (newCompletedSessions < currentState.totalSessions) {
                    currentState.currentSession + 1
                } else {
                    currentState.currentSession
                }

                _uiState.value = currentState.copy(
                    status = TimerStatus.IDLE,
                    completedSessions = newCompletedSessions,
                    currentSession = newCurrentSession,
                    remainingTimeMillis = currentState.totalTimeMillis,
                    progress = 100f
                )

                // Auto-switch to break mode
                if (newCompletedSessions < currentState.totalSessions) {
                    setMode(TimerMode.BREAK)
                }
            }
            TimerMode.BREAK -> {
                // Completed a break session, switch back to focus
                _uiState.value = currentState.copy(
                    status = TimerStatus.IDLE,
                    remainingTimeMillis = currentState.totalTimeMillis,
                    progress = 100f
                )
                setMode(TimerMode.FOCUS)
            }
        }
    }

    /**
     * Reset all sessions
     */
    fun resetSessions() {
        stopTimer()
        _uiState.value = _uiState.value.copy(
            completedSessions = 0,
            currentSession = 1
        )
    }

    /**
     * Format time as mm:ss
     */
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
