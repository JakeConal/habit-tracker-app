package com.example.habittracker.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import com.example.habittracker.data.api.QuoteApiService
import com.example.habittracker.databinding.FragmentDailyQuoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * DailyQuoteFragment - Screen for editing daily motivational quote
 */
class DailyQuoteFragment : Fragment() {

    private var _binding: FragmentDailyQuoteBinding? = null
    private val binding get() = _binding!!

    private var currentApiQuote: String = "Believe in yourself and all that you are."

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.dailyquotes.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val quoteApiService by lazy {
        retrofit.create(QuoteApiService::class.java)
    }

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

        // Load cached API quote
        currentApiQuote = sharedPref.getString("api_quote", "Believe in yourself and all that you are.")!!

        binding.switchShowOnDashboard.isChecked = showOnDashboard
        binding.switchUseSystemQuotes.isChecked = useSystemQuotes

        // Always enable custom quote input
        binding.etCustomQuote.isEnabled = true
        binding.etCustomQuote.isFocusable = true
        binding.etCustomQuote.isClickable = true
        binding.btnRefreshQuote.isEnabled = useSystemQuotes

        // Always load custom quote into EditText
        binding.etCustomQuote.setText(savedQuote)

        // Priority: Show custom quote if not empty, otherwise show API quote
        if (!savedQuote.isNullOrEmpty()) {
            binding.tvPreviewQuote.text = savedQuote
        } else {
            binding.tvPreviewQuote.text = currentApiQuote
        }

        updateCharCount()
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            if (validateAndSaveQuote()) {
                findNavController().navigateUp()
            }
        }

        // Refresh quote button
        binding.btnRefreshQuote.setOnClickListener {
            if (binding.switchUseSystemQuotes.isChecked) {
                refreshQuoteFromAPI()
            }
        }

        // Text watcher for character count and preview update
        binding.etCustomQuote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCharCount()
                // Always update preview with custom quote when typing
                val text = s?.toString()?.trim() ?: ""
                binding.tvPreviewQuote.text = if (text.isEmpty()) {
                    currentApiQuote
                } else {
                    text
                }
            }
        })

        // Switch listeners
        binding.switchShowOnDashboard.setOnCheckedChangeListener { _, _ ->
            // Show/hide quote on dashboard preference
        }

        binding.switchUseSystemQuotes.setOnCheckedChangeListener { _, isChecked ->
            binding.btnRefreshQuote.isEnabled = isChecked

            if (isChecked) {
                // Switching TO system quotes: show API quote in preview
                binding.tvPreviewQuote.text = currentApiQuote
            } else {
                // Switching TO custom quotes: show custom quote in preview
                val customQuote = binding.etCustomQuote.text.toString().trim()
                binding.tvPreviewQuote.text = if (customQuote.isEmpty()) {
                    currentApiQuote
                } else {
                    customQuote
                }
            }
        }
    }

    private fun updateCharCount() {
        val length = binding.etCustomQuote.text?.length ?: 0
        binding.tvCharCount.text = "$length/100"

        // Change color to warning when nearing limit (80+ chars)
        if (length >= 80) {
            binding.tvCharCount.setTextColor(requireContext().resources.getColor(android.R.color.holo_red_light, null))
        } else {
            binding.tvCharCount.setTextColor(requireContext().resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun validateAndSaveQuote(): Boolean {
        // Check if user needs to enter a custom quote when system quotes are off
        if (!binding.switchUseSystemQuotes.isChecked) {
            val customQuote = binding.etCustomQuote.text.toString().trim()
            if (customQuote.isEmpty()) {
                // Show warning
                Toast.makeText(
                    requireContext(),
                    "Please enter a custom quote or enable system quotes",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        // Save the settings
        saveQuoteSettings()
        Toast.makeText(requireContext(), "Quote saved successfully!", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun refreshQuoteFromAPI() {
        binding.tvPreviewQuote.text = "Loading..."
        binding.btnRefreshQuote.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    quoteApiService.getMotivationalQuote()
                }
                if (response.isSuccessful) {
                    response.body()?.let { quoteResponse ->
                        // Update API quote and preview
                        currentApiQuote = quoteResponse.quote
                        binding.tvPreviewQuote.text = quoteResponse.quote

                        // Clear custom quote in UI and in SharedPreferences so API quote is used
                        binding.etCustomQuote.setText("")
                        val sharedPref = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            remove("custom_quote")
                            putString("api_quote", currentApiQuote)
                            putLong("last_quote_fetch_time", System.currentTimeMillis())
                            // Mark that system quotes are enabled so API quote is active
                            putBoolean("use_system_quotes", true)
                            apply()
                        }

                        // Update switch UI to reflect that we're now using system quotes
                        binding.switchUseSystemQuotes.isChecked = true

                    } ?: run {
                        currentApiQuote = "Believe in yourself and all that you are."
                        binding.tvPreviewQuote.text = currentApiQuote
                    }
                } else {
                    currentApiQuote = "Believe in yourself and all that you are."
                    binding.tvPreviewQuote.text = currentApiQuote
                }
            } catch (_: Exception) {
                currentApiQuote = "Believe in yourself and all that you are."
                binding.tvPreviewQuote.text = currentApiQuote
            } finally {
                binding.btnRefreshQuote.isEnabled = true
            }
        }
    }

    private fun saveQuoteSettings() {
        val sharedPref = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("show_quote_on_dashboard", binding.switchShowOnDashboard.isChecked)
            putBoolean("use_system_quotes", binding.switchUseSystemQuotes.isChecked)

            // Always save custom quote from EditText
            val customQuote = binding.etCustomQuote.text.toString().trim()
            putString("custom_quote", customQuote)

            // Always save current API quote
            putString("api_quote", currentApiQuote)
            putLong("last_quote_fetch_time", System.currentTimeMillis())

            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
