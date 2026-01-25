package com.example.habittracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HabitStatistics(
    val habitId: String,
    val habitName: String,
    val completionRate: Float,
    val iconRes: Int,
    val iconBgRes: Int,
    val frequency: String,
    val totalCompletions: Int,
    val expectedCompletions: Int,
    val weeklyDays: List<Pair<String, DayStatus>> // Date to status mapping
)

enum class DayStatus {
    COMPLETED,
    MISSED,
    UPCOMING
}

data class WeeklyChartData(
    val dayName: String,
    val completionCount: Int,
    val totalHabits: Int,
    val completionRate: Float
)

data class CalendarData(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletionDays: Int,
    val monthlyCompletionRate: Float
)

class StatisticsViewModel : ViewModel() {

    private val habitRepository = HabitRepository.getInstance()
    private val categoryRepository = CategoryRepository.getInstance()
    private val authRepository = AuthRepository.getInstance()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    val categories: StateFlow<Map<String, Category>> = _categories.asStateFlow()

    private val _habitStatistics = MutableStateFlow<List<HabitStatistics>>(emptyList())
    val habitStatistics: StateFlow<List<HabitStatistics>> = _habitStatistics.asStateFlow()

    private val _weeklyChartData = MutableStateFlow<List<WeeklyChartData>>(emptyList())
    val weeklyChartData: StateFlow<List<WeeklyChartData>> = _weeklyChartData.asStateFlow()

    private val _calendarData = MutableStateFlow<CalendarData?>(null)
    val calendarData: StateFlow<CalendarData?> = _calendarData.asStateFlow()

    private val _overallCompletionRate = MutableStateFlow(0f)
    val overallCompletionRate: StateFlow<Float> = _overallCompletionRate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authRepository.getCurrentUser()?.uid ?: return@launch

                // Load habits and categories
                val habits = habitRepository.getHabitsForUser(userId)
                _habits.value = habits

                val categories = categoryRepository.getCategoriesForUser(userId)
                _categories.value = categories.associateBy { it.id }

                // Calculate statistics
                calculateHabitStatistics(habits)
                calculateWeeklyChartData(habits)
                calculateCalendarData(habits)
                calculateOverallCompletionRate(habits)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateHabitStatistics(habits: List<Habit>) {
        val statistics = habits.map { habit ->
            val category = _categories.value[habit.categoryId]

            // Calculate expected completions based on frequency
            val expectedCompletions = calculateExpectedCompletions(habit)
            val actualCompletions = habit.completedDates.size
            val completionRate = if (expectedCompletions > 0) {
                (actualCompletions.toFloat() / expectedCompletions) * 100f
            } else {
                0f
            }

            // Calculate weekly days status
            val weeklyDays = getWeeklyDaysForHabit(habit)
            val daily = habit.frequency.size == 7

            HabitStatistics(
                habitId = habit.id,
                habitName = habit.name,
                completionRate = completionRate.coerceIn(0f, 100f),
                iconRes = category?.icon?.resId ?: com.example.habittracker.R.drawable.ic_other,
                iconBgRes = category?.color?.resId ?: com.example.habittracker.R.drawable.bg_category_icon_pink_light,
                frequency = if (daily) "Daily" else habit.frequency.joinToString(", "),
                totalCompletions = actualCompletions,
                expectedCompletions = expectedCompletions,
                weeklyDays = weeklyDays
            )
        }.sortedByDescending { it.completionRate }

        _habitStatistics.value = statistics
    }

    private fun getWeeklyDaysForHabit(habit: Habit): List<Pair<String, DayStatus>> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(calendar.time)

        // Start from last Sunday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val weeklyDays = mutableListOf<Pair<String, DayStatus>>()

        for (i in 0..6) {
            val dateStr = dateFormat.format(calendar.time)
            val isCompleted = habit.completedDates.contains(dateStr)
            val shouldDoHabit = shouldHabitBeDoneOnDay(habit, calendar)
            val isFuture = dateStr > today

            val status = when {
                isCompleted -> DayStatus.COMPLETED
                isFuture || !shouldDoHabit -> DayStatus.UPCOMING
                else -> DayStatus.MISSED
            }

            weeklyDays.add(dateStr to status)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weeklyDays
    }

    private fun calculateExpectedCompletions(habit: Habit): Int {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val createdDate = Calendar.getInstance().apply {
            timeInMillis = habit.createdAt
        }.time

        val daysSinceCreation = ((today.time - createdDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1

        return when {
            habit.frequency.contains("Daily") -> daysSinceCreation
            habit.frequency.isEmpty() -> 0
            else -> {
                // Count how many days per week
                val daysPerWeek = habit.frequency.size
                val weeks = daysSinceCreation / 7
                val remainingDays = daysSinceCreation % 7
                (weeks * daysPerWeek) + minOf(daysPerWeek, remainingDays)
            }
        }
    }

    private fun calculateWeeklyChartData(habits: List<Habit>) {
        val calendar = Calendar.getInstance()

        val weekData = mutableListOf<WeeklyChartData>()
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        // Go back to last Sunday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        for (i in 0..6) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayName = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]

            // Count completions for this day
            var completionCount = 0
            var totalHabitsForDay = 0

            habits.forEach { habit ->
                // Check if habit should be done on this day
                val shouldDoHabit = shouldHabitBeDoneOnDay(habit, calendar)
                if (shouldDoHabit) {
                    totalHabitsForDay++
                    if (habit.completedDates.contains(dateStr)) {
                        completionCount++
                    }
                }
            }

            val completionRate = if (totalHabitsForDay > 0) {
                (completionCount.toFloat() / totalHabitsForDay) * 100f
            } else {
                0f
            }

            weekData.add(
                WeeklyChartData(
                    dayName = dayName,
                    completionCount = completionCount,
                    totalHabits = totalHabitsForDay,
                    completionRate = completionRate
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _weeklyChartData.value = weekData
    }

    private fun shouldHabitBeDoneOnDay(habit: Habit, calendar: Calendar): Boolean {
        if (habit.frequency.contains("Daily")) return true

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

    private fun calculateCalendarData(habits: List<Habit>) {
        if (habits.isEmpty()) {
            _calendarData.value = CalendarData(0, 0, 0, 0f)
            return
        }

        // Get all unique completion dates across all habits where at least one habit was completed
        val allCompletionDates = mutableSetOf<String>()
        val calendar = Calendar.getInstance()

        // Collect all dates where at least one habit was completed
        habits.forEach { habit ->
            habit.completedDates.forEach { dateStr ->
                allCompletionDates.add(dateStr)
            }
        }

        val sortedCompletionDates = allCompletionDates.sorted()

        // Calculate current streak
        val currentStreak = calculateCurrentStreak(sortedCompletionDates)

        // Calculate best streak
        val bestStreak = calculateBestStreak(sortedCompletionDates)

        // Total completion days
        val totalCompletionDays = sortedCompletionDates.size

        // Monthly completion rate
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val monthlyCompletions = sortedCompletionDates.count { it.startsWith(currentMonth) }
        val monthlyRate = (monthlyCompletions.toFloat() / daysInMonth) * 100f

        _calendarData.value = CalendarData(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompletionDays = totalCompletionDays,
            monthlyCompletionRate = monthlyRate
        )
    }

    private fun calculateCurrentStreak(completionDates: List<String>): Int {
        if (completionDates.isEmpty()) return 0

        val today = DateUtils.getCurrentDateString()
        val yesterday = DateUtils.getYesterdayDateString()

        if (!completionDates.contains(today) && !completionDates.contains(yesterday)) {
            return 0
        }

        var streak = 0
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (true) {
            val dateStr = dateFormat.format(calendar.time)
            if (completionDates.contains(dateStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateBestStreak(completionDates: List<String>): Int {
        if (completionDates.isEmpty()) return 0

        val sortedDates = completionDates.sorted()
        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val prevDate = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(sortedDates[i - 1])!!
            }
            val currDate = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(sortedDates[i])!!
            }

            val daysDiff = ((currDate.timeInMillis - prevDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            if (daysDiff == 1) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    private fun calculateOverallCompletionRate(habits: List<Habit>) {
        if (habits.isEmpty()) {
            _overallCompletionRate.value = 0f
            return
        }

        var totalExpected = 0
        var totalCompleted = 0

        habits.forEach { habit ->
            val expected = calculateExpectedCompletions(habit)
            totalExpected += expected
            totalCompleted += habit.completedDates.size
        }

        val rate = if (totalExpected > 0) {
            (totalCompleted.toFloat() / totalExpected) * 100f
        } else {
            0f
        }

        _overallCompletionRate.value = rate.coerceIn(0f, 100f)
    }
}
