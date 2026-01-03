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
        // Get username from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("HabitTrackerPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        
        // Set greeting text with username
        if (!username.isNullOrEmpty()) {
            binding.tvGreeting.text = "Hi, $username"
        } else {
            binding.tvGreeting.text = "Hi, User"
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
            // Show sample data if no habits
            val sampleHabits = getSampleHabits()
            habitsAdapter.updateHabits(sampleHabits)
        } else {
            // Convert repository Habit to UI Habit model
            val uiHabits = habitsList.map { habit ->
                Habit(
                    id = habit.id.toInt(),
                    name = habit.name,
                    isCompleted = habit.isCompleted,
                    // Use saved icon or fallback to category-based icon
                    iconRes = if (habit.iconRes != 0) habit.iconRes else getIconForCategory(habit.description),
                    iconBackgroundRes = if (habit.iconBackgroundRes != 0) habit.iconBackgroundRes else getBackgroundForCategory(habit.description),
                    progress = null
                )
            }
            habitsAdapter.updateHabits(uiHabits.toMutableList())
        }
    }

    private fun getIconForCategory(description: String): Int {
        return when {
            description.contains("Reading", ignoreCase = true) -> R.drawable.ic_book
            description.contains("Exercise", ignoreCase = true) || 
            description.contains("Walk", ignoreCase = true) -> R.drawable.ic_walk
            description.contains("Health", ignoreCase = true) -> R.drawable.ic_health
            description.contains("Work", ignoreCase = true) -> R.drawable.ic_work
            description.contains("Meditation", ignoreCase = true) -> R.drawable.ic_meditation
            else -> R.drawable.ic_other
        }
    }

    private fun getBackgroundForCategory(description: String): Int {
        return when {
            description.contains("Reading", ignoreCase = true) -> R.drawable.bg_habit_icon_pink
            description.contains("Exercise", ignoreCase = true) ||
            description.contains("Walk", ignoreCase = true) -> R.drawable.bg_habit_icon_coral
            description.contains("Health", ignoreCase = true) -> R.drawable.bg_habit_icon_orange
            else -> R.drawable.bg_habit_icon_pink
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
        val habits = mutableListOf<Habit>()
        habitsAdapter = HabitsAdapter(
            habits = habits,
            onHabitClick = { habit ->
                // Handle habit click - navigate to ViewHabit screen
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

    private fun getSampleHabits(): MutableList<Habit> {
        return mutableListOf(
            Habit(
                id = 1,
                name = "Praying Namaz",
                isCompleted = true,
                iconRes = R.drawable.ic_heart,
                iconBackgroundRes = R.drawable.bg_habit_icon_pink,
                progress = null
            ),
            Habit(
                id = 2,
                name = "Walk",
                isCompleted = true,
                iconRes = R.drawable.ic_walk,
                iconBackgroundRes = R.drawable.bg_habit_icon_coral,
                progress = null
            ),
            Habit(
                id = 3,
                name = "Drink The Water",
                isCompleted = false,
                iconRes = R.drawable.ic_water,
                iconBackgroundRes = R.drawable.bg_habit_icon_orange,
                progress = "800/2500 ML"
            )
        )
    }

    private fun toggleHabitCompletion(habit: Habit) {
        habit.isCompleted = !habit.isCompleted
        habitsAdapter.notifyDataSetChanged()
    }

    private fun navigateToViewHabit(habit: Habit) {
        // Navigate to ViewHabit screen with habit ID
        val bundle = Bundle().apply {
            putLong("habitId", habit.id.toLong())
        }
        findNavController().navigate(R.id.action_global_to_view_habit, bundle)
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

// Data classes
data class CalendarDay(
    val dayNumber: Int,
    val dayName: String,
    val isSelected: Boolean = false
)

data class Habit(
    val id: Int,
    val name: String,
    var isCompleted: Boolean,
    val iconRes: Int,
    val iconBackgroundRes: Int,
    val progress: String? = null
)
