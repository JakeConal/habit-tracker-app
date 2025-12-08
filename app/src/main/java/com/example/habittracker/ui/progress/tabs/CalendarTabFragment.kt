package com.example.habittracker.ui.progress.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittracker.databinding.FragmentCalendarTabBinding
import com.example.habittracker.ui.progress.adapter.CalendarDay
import com.example.habittracker.ui.progress.adapter.CalendarDayAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarTabFragment : Fragment() {

    private var _binding: FragmentCalendarTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CalendarDayAdapter
    private val calendar = Calendar.getInstance().apply {
        // Set to August 2024 for mockup
        set(Calendar.YEAR, 2024)
        set(Calendar.MONTH, Calendar.AUGUST)
    }

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
        setupStreakData()
        loadCalendarData()
        updateMonthYearDisplay()
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

    private fun setupStreakData() {
        binding.tvCurrentStreak.text = "1 Day"
        binding.tvBestStreak.text = "156 Days"
    }

    private fun loadCalendarData() {
        val days = mutableListOf<CalendarDay>()

        // Get first day of the month and calculate offset
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        // Add previous month days to fill the grid
        val prevMonth = calendar.clone() as Calendar
        prevMonth.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in dayOfWeek - 1 downTo 0) {
            days.add(CalendarDay(
                day = (daysInPrevMonth - i).toString(),
                isCurrentMonth = false,
                isSelected = false,
                backgroundColor = null
            ))
        }

        // Mock day colors for August 2024 display (hardcoded for mockup)
        val dayColors = mapOf(
            1 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            2 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            3 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            4 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            5 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            6 to com.example.habittracker.R.drawable.bg_calendar_day_selected_orange,
            7 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            8 to com.example.habittracker.R.drawable.bg_calendar_day_selected_orange,
            9 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            10 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            11 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            12 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            13 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            14 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            15 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            16 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            17 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green,
            18 to com.example.habittracker.R.drawable.bg_calendar_day_selected_orange,
            19 to com.example.habittracker.R.drawable.bg_calendar_day_selected_green
        )

        // Add current month days
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            days.add(CalendarDay(
                day = day.toString(),
                isCurrentMonth = true,
                isSelected = day == 1, // For mockup, highlight day 1
                backgroundColor = dayColors[day]
            ))
        }

        // Add next month days to fill the grid (complete weeks)
        val remainingSlots = 42 - days.size // 6 rows * 7 days
        for (day in 1..remainingSlots) {
            days.add(CalendarDay(
                day = day.toString(),
                isCurrentMonth = false,
                isSelected = false,
                backgroundColor = null
            ))
        }

        adapter.setItems(days)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CalendarTabFragment()
    }
}

