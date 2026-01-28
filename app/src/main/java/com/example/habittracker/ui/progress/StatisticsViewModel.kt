package com.example.habittracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.CategoryRepository
import com.example.habittracker.data.repository.HabitRepository
import com.example.habittracker.util.DateUtils
import com.example.habittracker.util.FrequencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    NOT_HABIT_DAY,
    HABIT_DAY,
    TODAY_PENDING
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

    private val _categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    val categories: StateFlow<Map<String, Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Fully reactive habits Flow - SharingStarted.Eagerly for preloading/caching
    val habits: StateFlow<List<Habit>> = habitRepository.habits
        .map { allHabits ->
            val currentTime = System.currentTimeMillis()
            allHabits.filter { habit ->
                if (habit.isChallengeHabit && habit.challengeDurationDays != null) {
                    val durationMillis = habit.challengeDurationDays.toLong() * 24 * 60 * 60 * 1000
                    val expiryTime = habit.createdAt + durationMillis
                    currentTime <= expiryTime
                } else {
                    true
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Combine habits and categories to calculate list statistics
    val habitStatistics: StateFlow<List<HabitStatistics>> = combine(habits, _categories) { filteredHabits, categoriesMap ->
        try {
            calculateHabitStatisticsInternal(filteredHabits, categoriesMap)
        } catch (e: Exception) {
            android.util.Log.e("StatisticsViewModel", "Error calculating habit statistics", e)
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Weekly chart data logic
    val weeklyChartData: StateFlow<List<WeeklyChartData>> = habits.map { habitsList ->
        try {
            calculateWeeklyChartDataInternal(habitsList)
        } catch (e: Exception) {
            android.util.Log.e("StatisticsViewModel", "Error calculating chart data", e)
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Calendar logic
    val calendarData: StateFlow<CalendarData?> = habits.map { habitsList ->
        try {
            calculateCalendarDataInternal(habitsList)
        } catch (e: Exception) {
            android.util.Log.e("StatisticsViewModel", "Error calculating calendar data", e)
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Overall completion rate
    val overallCompletionRate: StateFlow<Float> = habits.map { habitsList ->
        try {
            calculateOverallCompletionRateInternal(habitsList)
        } catch (e: Exception) {
            android.util.Log.e("StatisticsViewModel", "Error calculating completion rate", e)
            0f
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid ?: return@launch
            _isLoading.value = true
            try {
                // Fetch habits (updates habitRepository.habits Flow)
                habitRepository.getHabitsForUser(userId)

                // Fetch categories
                val categories = categoryRepository.getCategoriesForUser(userId)
                _categories.value = categories.associateBy { it.id }
            } catch (e: Exception) {
                android.util.Log.e("StatisticsViewModel", "Error loading data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateHabitStatisticsInternal(habits: List<Habit>, categoriesMap: Map<String, Category>): List<HabitStatistics> {
        return habits.map { habit ->
            val category = categoriesMap[habit.categoryId]

            // Calculate expected completions based on frequency
            val expectedCompletions = calculateExpectedCompletions(habit)
            val actualCompletions = habit.completedDates.size
            val completionRate = if (expectedCompletions > 0) {
                (actualCompletions.toFloat() / expectedCompletions) * 100f
            } else {
                0f
            }

            HabitStatistics(
                habitId = habit.id,
                habitName = habit.name,
                completionRate = completionRate.coerceIn(0f, 100f),
                iconRes = category?.icon?.resId ?: com.example.habittracker.R.drawable.ic_other,
                iconBgRes = category?.color?.resId ?: com.example.habittracker.R.drawable.bg_category_icon_pink_light,
                frequency = FrequencyFormatter.formatFrequency(habit.frequency),
                totalCompletions = actualCompletions,
                expectedCompletions = expectedCompletions,
                weeklyDays = getWeeklyDaysForHabit(habit)
            )
        }.sortedByDescending { it.completionRate }
    }

    private fun getWeeklyDaysForHabit(habit: Habit): List<Pair<String, DayStatus>> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(calendar.time)
        val habitCreatedDate = dateFormat.format(java.util.Date(habit.createdAt))

        // Start from last Sunday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val weeklyDays = mutableListOf<Pair<String, DayStatus>>()

        for (i in 0..6) {
            val dateStr = dateFormat.format(calendar.time)
            val isCompleted = habit.completedDates.contains(dateStr)
            val shouldDoHabit = shouldHabitBeDoneOnDay(habit, calendar)
            val isFuture = dateStr > today
            val isToday = dateStr == today

            val status = when {
                dateStr < habitCreatedDate -> DayStatus.NOT_HABIT_DAY // Before creation
                isCompleted -> DayStatus.COMPLETED
                !shouldDoHabit -> DayStatus.NOT_HABIT_DAY
                isToday && !isCompleted -> DayStatus.TODAY_PENDING
                isFuture -> DayStatus.HABIT_DAY
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

    private fun calculateWeeklyChartDataInternal(habits: List<Habit>): List<WeeklyChartData> {
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
                if (shouldHabitBeDoneOnDay(habit, calendar)) {
                    totalHabitsForDay++
                    if (habit.completedDates.contains(dateStr)) {
                        completionCount++
                    }
                }
            }

            weekData.add(
                WeeklyChartData(
                    dayName = dayName,
                    completionCount = completionCount,
                    totalHabits = totalHabitsForDay,
                    completionRate = if (totalHabitsForDay > 0) (completionCount.toFloat() / totalHabitsForDay) * 100f else 0f
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekData
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

    private fun calculateCalendarDataInternal(habits: List<Habit>): CalendarData? {
        if (habits.isEmpty()) return CalendarData(0, 0, 0, 0f)

        val allCompletionDates = mutableSetOf<String>()
        habits.forEach { habit -> habit.completedDates.forEach { allCompletionDates.add(it) } }
        val sortedCompletionDates = allCompletionDates.sorted()

        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val monthlyCompletions = sortedCompletionDates.count { it.startsWith(currentMonth) }

        return CalendarData(
            currentStreak = calculateCurrentStreak(sortedCompletionDates),
            bestStreak = calculateBestStreak(sortedCompletionDates),
            totalCompletionDays = sortedCompletionDates.size,
            monthlyCompletionRate = (monthlyCompletions.toFloat() / daysInMonth) * 100f
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

    private fun calculateOverallCompletionRateInternal(habits: List<Habit>): Float {
        if (habits.isEmpty()) return 0f
        var totalExpected = 0
        var totalCompleted = 0
        habits.forEach { habit ->
            totalExpected += calculateExpectedCompletions(habit)
            totalCompleted += habit.completedDates.size
        }
        return if (totalExpected > 0) (totalCompleted.toFloat() / totalExpected) * 100f else 0f
    }
}
