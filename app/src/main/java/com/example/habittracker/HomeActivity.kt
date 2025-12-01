package com.example.habittracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habittracker.adapter.DateAdapter
import com.example.habittracker.adapter.HabitAdapter
import com.example.habittracker.databinding.ActivityHomeBinding
import com.example.habittracker.model.DateItem
import com.example.habittracker.model.Habit

/**
 * Home screen activity - Controller in MVC pattern.
 * Displays greeting, motivational quote, date selector, and habit list.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var dateAdapter: DateAdapter

    // Mutable list to track habit states
    private val habits = mutableListOf<Habit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.homeRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupGreeting()
        setupDateSelector()
        setupHabitsRecyclerView()
        loadFakeData()
    }

    /**
     * Sets up the greeting text with the user's name.
     */
    private fun setupGreeting() {
        val username = getString(R.string.default_username)
        binding.greetingText.text = getString(R.string.greeting_text, username)
    }

    /**
     * Initializes the horizontal date selector RecyclerView.
     */
    private fun setupDateSelector() {
        dateAdapter = DateAdapter { dateItem ->
            // Handle date selection - could be extended to load habits for that date
            onDateSelected(dateItem)
        }
        binding.dateSelector.adapter = dateAdapter

        // Load dates for the current week
        val dates = generateWeekDates()
        dateAdapter.submitList(dates)
    }

    /**
     * Generates date items for the current week.
     * In a real app, this would use actual calendar data.
     */
    private fun generateWeekDates(): List<DateItem> {
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val currentDayIndex = 4 // Simulating Thursday as "today"

        return dayNames.mapIndexed { index, dayName ->
            DateItem(
                dayOfWeek = dayName,
                dayNumber = (index + 1).toString(),
                isSelected = index == currentDayIndex
            )
        }
    }

    /**
     * Handles date selection from the date selector.
     */
    private fun onDateSelected(dateItem: DateItem) {
        // In a real app, this would load habits for the selected date
        // For now, just log the selection or update UI as needed
    }

    /**
     * Initializes the habits RecyclerView with adapter and click handling.
     */
    private fun setupHabitsRecyclerView() {
        habitAdapter = HabitAdapter { habit ->
            toggleHabitCompletion(habit)
        }
        binding.habitsRecyclerView.adapter = habitAdapter
    }

    /**
     * Loads fake habit data for demonstration.
     * In a real app, this would fetch from a data source.
     */
    private fun loadFakeData() {
        habits.clear()
        habits.addAll(
            listOf(
                Habit(id = 1, title = "Morning Exercise", isCompleted = false),
                Habit(id = 2, title = "Read for 30 minutes", isCompleted = true),
                Habit(id = 3, title = "Drink 8 glasses of water", isCompleted = false),
                Habit(id = 4, title = "Meditate for 10 minutes", isCompleted = false),
                Habit(id = 5, title = "Learn something new", isCompleted = true)
            )
        )
        updateHabitsList()
    }

    /**
     * Toggles the completion status of a habit.
     * Updates the data and refreshes the UI.
     */
    private fun toggleHabitCompletion(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            // Create a new habit with toggled completion state
            val updatedHabit = habit.copy(isCompleted = !habit.isCompleted)
            habits[index] = updatedHabit
            updateHabitsList()
        }
    }

    /**
     * Updates the adapter with the current habits list.
     */
    private fun updateHabitsList() {
        // Submit a copy of the list to trigger DiffUtil
        habitAdapter.submitList(habits.toList())
    }
}
