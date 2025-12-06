package com.example.habittracker.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base Fragment class that provides common functionality for all fragments.
 * Extend this class to inherit common behavior like loading states, error handling, etc.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    /**
     * Abstract method to get the ViewBinding instance.
     * Subclasses must implement this to provide their specific ViewBinding.
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Called after the view is created. Override to initialize views and data.
     */
    abstract fun setupView()

    /**
     * Called to observe ViewModel data changes. Override to set up observers.
     */
    open fun observeData() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a success message to the user
     */
    protected fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Handle common error states
     */
    protected fun handleError(throwable: Throwable) {
        hideLoading()
        showError(throwable.message ?: "An unknown error occurred")
    }
}

