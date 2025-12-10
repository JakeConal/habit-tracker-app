package com.example.habittracker.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentHomeBinding
import java.util.*

/**
 * HomeFragment - Main screen displaying dashboard of the application
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
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
        
        // Set content text - matching StaticFragment style
        binding.tvQuote.text = getString(R.string.motivational_quote)
        binding.tvHabitsTitle.text = getString(R.string.your_habits)
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
        val habits = getSampleHabits()
        habitsAdapter = HabitsAdapter(habits) { habit ->
            // Handle habit click
            toggleHabitCompletion(habit)
        }
        
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
