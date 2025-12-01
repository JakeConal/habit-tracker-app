package com.example.habittracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.databinding.ActivityHomeBinding

/**
 * Home Activity (Controller) - Main screen of the app
 * Displays greeting, motivational quote, date selector, and list of habits
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var dateAdapter: DateAdapter

    // In-memory list to track habit state (simulating a data layer)
    private val habitsList = mutableListOf<Habit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up ViewBinding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDateSelector()
        setupHabitsRecyclerView()
        loadFakeData()
    }

    /**
     * Initialize the horizontal date selector RecyclerView
     */
    private fun setupDateSelector() {
        dateAdapter = DateAdapter()

        binding.dateRecyclerView.apply {
            adapter = dateAdapter
            layoutManager = LinearLayoutManager(
                this@HomeActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        // Generate fake dates for the week
        val fakeDates = listOf(
            DateItem("14", "Sun"),
            DateItem("15", "Mon"),
            DateItem("16", "Tue"),
            DateItem("17", "Wed"),
            DateItem("18", "Thu"),
            DateItem("19", "Fri"),
            DateItem("20", "Sat")
        )
        dateAdapter.submitList(fakeDates)
    }

    /**
     * Initialize the habits RecyclerView with adapter and click handler
     */
    private fun setupHabitsRecyclerView() {
        // Create adapter with callback for completing habits
        habitAdapter = HabitAdapter { habit ->
            onHabitCompleted(habit)
        }

        binding.habitsRecyclerView.apply {
            adapter = habitAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    /**
     * Load fake habit data for demonstration
     */
    private fun loadFakeData() {
        habitsList.clear()
        habitsList.addAll(
            listOf(
                Habit(
                    id = 1,
                    title = "Praying Namaz",
                    icon = "🙏",
                    isCompleted = false
                ),
                Habit(
                    id = 2,
                    title = "Walk",
                    icon = "🚶",
                    isCompleted = false
                ),
                Habit(
                    id = 3,
                    title = "Drink The Water",
                    icon = "💧",
                    isCompleted = false
                ),
                Habit(
                    id = 4,
                    title = "Reading a book",
                    icon = "📚",
                    isCompleted = false
                )
            )
        )

        habitAdapter.submitList(habitsList)
    }

    /**
     * Handle habit completion - toggles the completed state and updates UI
     * This is the business logic for marking a habit as complete
     */
    private fun onHabitCompleted(habit: Habit) {
        // Find and update the habit in our list
        val index = habitsList.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val updatedHabit = habit.copy(isCompleted = true)
            habitsList[index] = updatedHabit

            // Update only the changed item in the adapter
            habitAdapter.updateHabit(updatedHabit)

            // In a real app, you would also persist this to a database here
            // e.g., habitRepository.updateHabit(updatedHabit)
        }
    }
}

