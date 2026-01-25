package com.example.habittracker.ui.progress.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentCalendarTabBinding
import com.example.habittracker.ui.progress.StatisticsViewModel
import com.example.habittracker.ui.progress.adapter.CalendarDay
import com.example.habittracker.ui.progress.adapter.CalendarDayAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarTabFragment : Fragment() {

    private var _binding: FragmentCalendarTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CalendarDayAdapter
    private val viewModel: StatisticsViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        observeData()
        loadCalendarData()
        updateMonthYearDisplay()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when tab becomes visible
        viewModel.loadAllData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.calendarData.collect { data ->
                data?.let {
                    binding.tvCurrentStreak.text =
                        "${it.currentStreak} Day${if (it.currentStreak != 1) "s" else ""}"
                    binding.tvBestStreak.text =
                        "${it.bestStreak} Day${if (it.bestStreak != 1) "s" else ""}"
                    loadCalendarData()
                }
            }
        }
    }

    private fun setupCalendar() {
        adapter = CalendarDayAdapter()
        binding.rvCalendarDays.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = this@CalendarTabFragment.adapter
        }

        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthYearDisplay()
            loadCalendarData()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthYearDisplay()
            loadCalendarData()
        }
    }

    private fun updateMonthYearDisplay() {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthYear = "${monthFormat.format(calendar.time)}\n${yearFormat.format(calendar.time)}"
        binding.tvMonthYear.text = monthYear
    }

    private fun loadCalendarData() {
        val days = mutableListOf<CalendarDay>()
        val habits = viewModel.habits.value

        // Get first day of the month and calculate offset
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        // Add previous month days to fill the grid
        val prevMonth = calendar.clone() as Calendar
        prevMonth.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in dayOfWeek - 1 downTo 0) {
            days.add(
                CalendarDay(
                    day = (daysInPrevMonth - i).toString(),
                    isCurrentMonth = false,
                    isSelected = false,
                    backgroundColor = null
                )
            )
        }

        // Add current month days with real data
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (day in 1..daysInMonth) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(dayCalendar.time)

            // Count completions and misses for this day
            var completedCount = 0
            var missedCount = 0
            var totalScheduledHabits = 0

            habits.forEach { habit ->
                val shouldDoHabit = shouldHabitBeDoneOnDay(habit, dayCalendar)
                if (shouldDoHabit && !isDateInFuture(dateStr)) {
                    totalScheduledHabits++
                    if (habit.completedDates.contains(dateStr)) {
                        completedCount++
                    } else {
                        missedCount++
                    }
                }
            }

            val backgroundColor = when {
                totalScheduledHabits == 0 -> null
                completedCount == totalScheduledHabits -> R.drawable.bg_calendar_day_selected_green
                missedCount > 0 && completedCount > 0 -> R.drawable.bg_calendar_day_selected_orange
                missedCount == totalScheduledHabits -> R.drawable.bg_calendar_day_selected_red
                else -> null
            }

            days.add(
                CalendarDay(
                    day = day.toString(),
                    isCurrentMonth = true,
                    isSelected = false,
                    backgroundColor = backgroundColor
                )
            )
        }

        // Add next month days to fill the grid (complete weeks)
        val remainingSlots = 42 - days.size // 6 rows * 7 days
        for (day in 1..remainingSlots) {
            days.add(
                CalendarDay(
                    day = day.toString(),
                    isCurrentMonth = false,
                    isSelected = false,
                    backgroundColor = null
                )
            )
        }

        adapter.setItems(days)
    }

    private fun shouldHabitBeDoneOnDay(
        habit: com.example.habittracker.data.model.Habit,
        calendar: Calendar
    ): Boolean {
        // Check if the habit was created before or on this day
        val habitCreatedDate = Calendar.getInstance().apply {
            timeInMillis = habit.createdAt
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val checkDate = calendar.clone() as Calendar
        checkDate.set(Calendar.HOUR_OF_DAY, 0)
        checkDate.set(Calendar.MINUTE, 0)
        checkDate.set(Calendar.SECOND, 0)
        checkDate.set(Calendar.MILLISECOND, 0)

        // Habit must be created on or before the check date
        if (checkDate.before(habitCreatedDate)) {
            return false
        }

        // Check if habit should be done on this day based on frequency
        if (habit.frequency.contains("Daily")) {
            return true
        }

        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }

        return habit.frequency.contains(dayOfWeek)
    }

    private fun isDateInFuture(dateStr: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Calendar.getInstance().time)
        return dateStr > today
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CalendarTabFragment()
    }
}

