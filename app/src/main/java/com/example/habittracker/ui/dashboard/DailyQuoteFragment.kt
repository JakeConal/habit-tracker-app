package com.example.habittracker.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.habittracker.databinding.FragmentDailyQuoteBinding

/**
 * DailyQuoteFragment - Screen for editing daily motivational quote
 */
class DailyQuoteFragment : Fragment() {

    private var _binding: FragmentDailyQuoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyQuoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupListeners()
    }

    private fun setupView() {
        // Load saved quote from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)
        val savedQuote = sharedPref.getString("custom_quote", "")
        val showOnDashboard = sharedPref.getBoolean("show_quote_on_dashboard", true)
        val useSystemQuotes = sharedPref.getBoolean("use_system_quotes", false)

        if (!savedQuote.isNullOrEmpty()) {
            binding.etCustomQuote.setText(savedQuote)
            binding.tvPreviewQuote.text = savedQuote
        }

        binding.switchShowOnDashboard.isChecked = showOnDashboard
        binding.switchUseSystemQuotes.isChecked = useSystemQuotes

        updateCharCount()
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            saveQuoteSettings()
            findNavController().navigateUp()
        }

        // Text watcher for character count
        binding.etCustomQuote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCharCount()
                binding.tvPreviewQuote.text = s?.toString() ?: "Believe in yourself and all that you are."
            }
        })

        // Switch listeners
        binding.switchShowOnDashboard.setOnCheckedChangeListener { _, _ ->
            // Handle toggle
        }

        binding.switchUseSystemQuotes.setOnCheckedChangeListener { _, isChecked ->
            // If using system quotes, disable custom quote input
            binding.etCustomQuote.isEnabled = !isChecked
            if (isChecked) {
                binding.tvPreviewQuote.text = "Believe in yourself and all that you are."
            } else {
                binding.tvPreviewQuote.text = binding.etCustomQuote.text.toString()
            }
        }
    }

    private fun updateCharCount() {
        val length = binding.etCustomQuote.text?.length ?: 0
        binding.tvCharCount.text = "$length/100"
    }

    private fun saveQuoteSettings() {
        val sharedPref = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("custom_quote", binding.etCustomQuote.text.toString())
            putBoolean("show_quote_on_dashboard", binding.switchShowOnDashboard.isChecked)
            putBoolean("use_system_quotes", binding.switchUseSystemQuotes.isChecked)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
