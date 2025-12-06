package com.example.habittracker.ui.common

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * Base Activity class that provides common functionality for all activities.
 * Extend this class to inherit common behavior like loading states, error handling, etc.
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    /**
     * Abstract method to get the ViewBinding instance.
     * Subclasses must implement this to provide their specific ViewBinding.
     */
    abstract fun getViewBinding(): VB

    /**
     * Called after the view is created. Override to initialize views and data.
     */
    abstract fun setupView()

    /**
     * Called to observe ViewModel data changes. Override to set up observers.
     */
    open fun observeData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = getViewBinding()
        setContentView(binding.root)
        setupView()
        observeData()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Show a loading indicator
     */
    protected open fun showLoading() {
        // Override in subclass to show loading UI
    }

    /**
     * Hide the loading indicator
     */
    protected open fun hideLoading() {
        // Override in subclass to hide loading UI
    }

    /**
     * Show an error message to the user
     */
    protected fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a success message to the user
     */
    protected fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Handle common error states
     */
    protected fun handleError(throwable: Throwable) {
        hideLoading()
        showError(throwable.message ?: "An unknown error occurred")
    }
}

