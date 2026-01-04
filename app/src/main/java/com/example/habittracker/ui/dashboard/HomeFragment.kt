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
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
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
                    // You could also show user points or other info here
                    // binding.tvUserPoints.text = "${user.points} pts"
                } else {
                    binding.tvGreeting.text = "Hi, User"
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
        val useSystemQuotes = sharedPref.getBoolean("use_system_quotes", false)
        val showOnDashboard = sharedPref.getBoolean("show_quote_on_dashboard", true)
        
        if (!showOnDashboard) {
            binding.quoteCard.visibility = View.GONE
            return
        }
        
        binding.quoteCard.visibility = View.VISIBLE
        
        if (useSystemQuotes) {
            // Use default motivational quote
            binding.tvQuote.text = getString(R.string.motivational_quote)
        } else {
            // Use custom quote if available
            val customQuote = sharedPref.getString("custom_quote", null)
            if (!customQuote.isNullOrEmpty()) {
                binding.tvQuote.text = customQuote
            } else {
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
    }

    private fun updateHabits(habitsList: List<com.example.habittracker.data.model.Habit>) {
        if (habitsList.isEmpty()) {
            // No habits found - show empty state or sample data
            binding.rvHabits.visibility = View.VISIBLE // Keep visible to show empty state
            // Convert empty list to UI model
            habitsAdapter.updateHabits(mutableListOf(), viewModel.categories.value)
        } else {
            binding.rvHabits.visibility = View.VISIBLE
            habitsAdapter.updateHabits(habitsList.toMutableList(), viewModel.categories.value)
        }
    }

    private fun setupCalendar() {
        val days = generateCalendarDays()
        calendarAdapter = CalendarAdapter(days) { day ->
            // Handle day click
            calendarAdapter.setSelectedDay(day)
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
        
        // Start from 2 days before today to show 6 days total (matching Figma design)
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        
        for (i in 0..5) {
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
            
            val isToday = dayOfMonth == today && i == 2
            
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
            onHabitClick = { _ ->
                // TODO: Handle habit click - show dialog with habit details
                // navigateToViewHabit(habit)
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

    private fun setupClickListeners() {
        // Navigate to Daily Quote screen when quote card is clicked
        binding.quoteCard.setOnClickListener {
            findNavController().navigate(R.id.nav_daily_quote)
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
