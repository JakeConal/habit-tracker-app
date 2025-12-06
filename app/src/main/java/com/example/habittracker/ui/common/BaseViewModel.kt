package com.example.habittracker.ui.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel class that provides common functionality for all ViewModels.
 * Extend this class to inherit common behavior like loading states, error handling, etc.
 */
abstract class BaseViewModel : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Set loading state
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Set error message
     */
    protected fun setError(message: String?) {
        _error.value = message
    }

    /**
     * Clear error message
     */
    protected fun clearError() {
        _error.value = null
    }

    /**
     * Handle exceptions and set appropriate error states
     */
    protected fun handleException(exception: Throwable) {
        setLoading(false)
        setError(exception.message ?: "An unknown error occurred")
    }
}

/**
 * Sealed class representing UI states for data loading operations.
 * Use this to represent Loading, Success, and Error states in the UI.
 */
sealed class UiState<out T> {
    /**
     * Initial state before any data is loaded
     */
    object Initial : UiState<Nothing>()
    
    /**
     * Loading state while data is being fetched
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with loaded data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with error message
     */
    data class Error(val message: String) : UiState<Nothing>()
}

