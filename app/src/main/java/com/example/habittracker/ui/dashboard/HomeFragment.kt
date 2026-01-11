package com.example.habittracker.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.api.QuoteApiService
import com.example.habittracker.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

/**
 * HomeFragment - Main screen displaying dashboard of the application
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedDay: CalendarDay? = null

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupCalendar()
        setupHabits()
        setupClickListeners()
        observeData()
    }

    private fun setupView() {
        // Observe current user from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                if (user != null) {
                    // Set greeting text with user name from Firebase
                    binding.tvGreeting.text = "Hi, ${user.name}"
                    // Load user avatar from Firebase
                    user.avatarUrl?.let { url ->
                        Glide.with(this@HomeFragment)
                            .load(url)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.ivAvatar)
                    } ?: run {
                        // If no avatar URL, use default
                        binding.ivAvatar.setImageResource(R.drawable.ic_person)
                    }
                    // You could also show user points or other info here
                    // binding.tvUserPoints.text = "${user.points} pts"
                } else {
                    binding.tvGreeting.text = "Hi, User"
                    binding.ivAvatar.setImageResource(R.drawable.ic_person)
                }
            }
        }
        
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { _ ->
                // You can show/hide loading indicator here if needed
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Load custom quote or use default
        loadQuote()
        
        binding.tvHabitsTitle.text = getString(R.string.your_habits)
    }

    private fun loadQuote() {
        val sharedPref = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)
        val customQuote = sharedPref.getString("custom_quote", "")
        val useSystemQuotes = sharedPref.getBoolean("use_system_quotes", false)
        val showOnDashboard = sharedPref.getBoolean("show_quote_on_dashboard", true)
        
        if (!showOnDashboard) {
            binding.quoteCard.visibility = View.GONE
            return
        }
        
        binding.quoteCard.visibility = View.VISIBLE
        
        // Priority: Show custom quote if not empty, otherwise show API quote
        if (!customQuote.isNullOrEmpty()) {
            binding.tvQuote.text = customQuote
        } else if (useSystemQuotes) {
            // Check if we need to fetch a new quote (once per day)
            val lastFetchTime = sharedPref.getLong("last_quote_fetch_time", 0L)
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000
            val shouldRefresh = (currentTime - lastFetchTime) >= oneDayInMillis

            if (shouldRefresh) {
                // Fetch new quote from API
                fetchAndDisplayQuote(sharedPref)
            } else {
                // Use cached quote
                val cachedQuote = sharedPref.getString("api_quote", "Believe in yourself and all that you are.")
                binding.tvQuote.text = cachedQuote
            }
        } else {
            // No custom quote and system quotes disabled - use default
            binding.tvQuote.text = getString(R.string.motivational_quote)
        }
    }

    private fun fetchAndDisplayQuote(sharedPref: android.content.SharedPreferences) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    quoteApiService.getMotivationalQuote()
                }
                if (response.isSuccessful) {
                    response.body()?.let { quoteResponse ->
                        binding.tvQuote.text = quoteResponse.quote
                        // Cache the quote and update fetch time
                        with(sharedPref.edit()) {
                            putString("api_quote", quoteResponse.quote)
                            putLong("last_quote_fetch_time", System.currentTimeMillis())
                            apply()
                        }
                    } ?: run {
                        binding.tvQuote.text = getString(R.string.motivational_quote)
                    }
                } else {
                    binding.tvQuote.text = getString(R.string.motivational_quote)
                }
            } catch (_: Exception) {
                binding.tvQuote.text = getString(R.string.motivational_quote)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload quote when returning from Daily Quote screen
        loadQuote()
        // Reload habits when returning to home screen
        viewModel.loadHabits()
    }

    private fun observeData() {
        // Observe habits from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collect { habitsList ->
                updateHabits(habitsList)
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    showError(it)
                }
            }
        }
    }

    private fun updateHabits(habitsList: List<com.example.habittracker.data.model.Habit>) {
        val filteredHabits = selectedDay?.let { day ->
            habitsList.filter { habit ->
                val fullDayName = getFullDayName(day.dayName)
                habit.frequency.contains("Daily") || habit.frequency.contains(fullDayName)
            }
        } ?: habitsList

        if (filteredHabits.isEmpty()) {
            // No habits found - show empty state or sample data
            binding.rvHabits.visibility = View.VISIBLE // Keep visible to show empty state
            // Convert empty list to UI model
            habitsAdapter.updateHabits(mutableListOf(), viewModel.categories.value)
        } else {
            binding.rvHabits.visibility = View.VISIBLE
            habitsAdapter.updateHabits(filteredHabits.toMutableList(), viewModel.categories.value)
        }
    }

    private fun setupCalendar() {
        val days = generateCalendarDays()
        selectedDay = days.find { it.isSelected }
        calendarAdapter = CalendarAdapter(days) { day ->
            // Handle day click
            selectedDay = day
            calendarAdapter.setSelectedDay(day)
            updateHabits(viewModel.habits.value)
        }
        
        binding.rvCalendar.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = calendarAdapter
        }
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Start from 3 days before today to show 7 days total (today in the middle)
        calendar.add(Calendar.DAY_OF_MONTH, -3)

        for (i in 0..6) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            // Get abbreviated day name (Sun, Mon, Tue, etc.)
            val dayName = when (dayOfWeek) {
                Calendar.SUNDAY -> "Sun"
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                else -> ""
            }
            
            val isToday = dayOfMonth == today && i == 3

            days.add(CalendarDay(dayOfMonth, dayName, isToday))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return days
    }

    private fun setupHabits() {
        val habits = mutableListOf<com.example.habittracker.data.model.Habit>()
        habitsAdapter = HabitsAdapter(
            habits = habits,
            categories = viewModel.categories.value,
            onHabitClick = { habit ->
                // Navigate to ViewHabitFragment to view/edit habit details
                navigateToViewHabit(habit)
            },
            onCheckClick = { habit ->
                // Handle check button click - toggle completion
                toggleHabitCompletion(habit)
            }
        )
        
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitsAdapter
        }
    }

    private fun toggleHabitCompletion(habit: com.example.habittracker.data.model.Habit) {
        // Update in Firestore via ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toggleHabitCompletion(habit)
        }
    }

    private fun navigateToViewHabit(habit: com.example.habittracker.data.model.Habit) {
        // Navigate to ViewHabitFragment with habitId
        val bundle = Bundle().apply {
            putString("habitId", habit.id)
        }
        findNavController().navigate(R.id.action_global_to_view_habit, bundle)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        // Navigate to Daily Quote screen when quote card is clicked
        binding.quoteCard.setOnClickListener {
            findNavController().navigate(R.id.nav_daily_quote)
        }
    }

    private fun getFullDayName(shortName: String): String {
        return when (shortName) {
            "Sun" -> "Sunday"
            "Mon" -> "Monday"
            "Tue" -> "Tuesday"
            "Wed" -> "Wednesday"
            "Thu" -> "Thursday"
            "Fri" -> "Friday"
            "Sat" -> "Saturday"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
