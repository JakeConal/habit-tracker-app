package com.example.habittracker.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.service.QuoteApiService
import com.example.habittracker.data.model.Habit
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.data.repository.PostRepository
import com.example.habittracker.databinding.FragmentHomeBinding
import com.example.habittracker.databinding.LayoutNotificationDropdownBinding
import com.example.habittracker.ui.habit.detail.ViewHabitDetailActivity
import com.example.habittracker.ui.notification.NotificationDropdownAdapter
import com.example.habittracker.ui.notification.NotificationViewModel
import com.example.habittracker.ui.feed.CommentsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeFragment - Main screen displaying dashboard of the application
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by activityViewModels()

    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedDay: CalendarDay? = null

    private var notificationPopupWindow: PopupWindow? = null
    private lateinit var notificationAdapter: NotificationDropdownAdapter

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
        setupNotificationAdapter()
        setupClickListeners()
        observeData()
    }

    private fun setupNotificationAdapter() {
        notificationAdapter = NotificationDropdownAdapter { notification ->
            // Handle notification click
            notificationViewModel.markNotificationAsRead(notification.id)
            notificationPopupWindow?.dismiss()

            // Navigate to post
            if (notification.postId.isNotEmpty()) {
                navigateToPost(notification.postId)
            }
        }
    }

    private fun navigateToPost(postId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val post = PostRepository.getInstance().getPostById(postId)
            if (post != null) {
                val intent = android.content.Intent(requireContext(), CommentsActivity::class.java).apply {
                    putExtra(CommentsActivity.EXTRA_POST_ID, post.id)
                    putExtra(CommentsActivity.EXTRA_POST_USER_ID, post.userId)
                    putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.authorName)
                    putExtra(CommentsActivity.EXTRA_AUTHOR_AVATAR, post.authorAvatarUrl)
                    putExtra(CommentsActivity.EXTRA_TIMESTAMP, post.timestamp)
                    putExtra(CommentsActivity.EXTRA_CONTENT, post.content)
                    putExtra(CommentsActivity.EXTRA_IMAGE_URL, post.imageUrl)
                    putExtra(CommentsActivity.EXTRA_LIKES_COUNT, post.likeCount)
                    putExtra(CommentsActivity.EXTRA_COMMENTS_COUNT, post.commentCount)
                    putExtra(CommentsActivity.EXTRA_IS_LIKED, post.likedBy.contains(AuthRepository.getInstance().getCurrentUser()?.uid))

                    // Add shared post data for full view
                    if (!post.originalPostId.isNullOrEmpty()) {
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_POST_ID, post.originalPostId)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_USER_ID, post.originalUserId)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_NAME, post.originalAuthorName)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_AUTHOR_AVATAR, post.originalAuthorAvatarUrl)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_CONTENT, post.originalContent)
                        putExtra(CommentsActivity.EXTRA_ORIGINAL_IMAGE_URL, post.originalImageUrl)
                    }
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNotificationDropdown(anchorView: View) {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)

        val dropdownBinding = LayoutNotificationDropdownBinding.inflate(inflater)

        dropdownBinding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }

        // Initially show only 5 or all based on state
        updateNotificationList(dropdownBinding)

        dropdownBinding.btnSeeMore.setOnClickListener {
            notificationViewModel.setShowAllNotifications(true)
            updateNotificationList(dropdownBinding)
        }

        notificationPopupWindow = PopupWindow(
            dropdownBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 10f
            setBackgroundDrawable(null)
            showAsDropDown(anchorView, 0, 10, android.view.Gravity.END)
        }
    }

    private fun updateNotificationList(dropdownBinding: LayoutNotificationDropdownBinding) {
        val notifications = notificationViewModel.notifications.value
        val showAll = notificationViewModel.showAllNotifications.value

        if (notifications.size > 5 && !showAll) {
            notificationAdapter.submitList(notifications.take(5))
            dropdownBinding.btnSeeMore.visibility = View.VISIBLE
        } else {
            notificationAdapter.submitList(notifications)
            dropdownBinding.btnSeeMore.visibility = View.GONE
        }
    }

    private fun setupView() {
        // Observe current user from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                if (user != null) {
                    // Set greeting text with user name from Firebase
                    binding.tvGreeting.text = getString(R.string.greeting_default, user.name)
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
                } else {
                    binding.tvGreeting.text = getString(R.string.greeting_default, "User")
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
        // Observe both habits and categories together to ensure icons are always updated correctly
        viewLifecycleOwner.lifecycleScope.launch {
            combine(viewModel.habits, viewModel.categories) { habitsList, categoriesList ->
                Pair(habitsList, categoriesList)
            }.collect { (habitsList, categoriesList) ->
                updateHabits(habitsList, categoriesList)
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

        // Observe notifications for badge or updating the open dropdown
        viewLifecycleOwner.lifecycleScope.launch {
            notificationViewModel.notifications.collect { notifications ->
                // Update popup if it's showing
                notificationPopupWindow?.let { popup ->
                    if (popup.isShowing) {
                        notificationAdapter.submitList(notifications)
                    }
                }
            }
        }

        // Observe unread count for badge
        viewLifecycleOwner.lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                if (count > 0) {
                    binding.tvNotificationBadge.text = count.toString()
                    binding.tvNotificationBadge.visibility = View.VISIBLE
                } else {
                    binding.tvNotificationBadge.visibility = View.GONE
                }
            }
        }
    }

    private fun updateHabits(habitsList: List<Habit>, categoriesList: List<com.example.habittracker.data.model.Category>) {
        val filteredHabits = selectedDay?.let { day ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            habitsList.filter { habit ->
                val fullDayName = getFullDayName(day.dayName)
                val habitDate = sdf.format(Date(habit.createdAt))
                val isAfterOrCreate = day.fullDate >= habitDate
                isAfterOrCreate && (habit.frequency.contains("Daily") || habit.frequency.contains(fullDayName))
            }
        } ?: habitsList

        if (filteredHabits.isEmpty()) {
            binding.rvHabits.visibility = View.GONE
            binding.llEmptyHabits.visibility = View.VISIBLE
        } else {
            binding.rvHabits.visibility = View.VISIBLE
            binding.llEmptyHabits.visibility = View.GONE
        }

        habitsAdapter.updateHabits(
            filteredHabits.toMutableList(),
            categoriesList,
            selectedDay?.fullDate ?: ""
        )
    }

    private fun setupCalendar() {
        val days = generateCalendarDays()
        selectedDay = days.find { it.isSelected }
        calendarAdapter = CalendarAdapter(days) { day ->
            // Handle day click
            selectedDay = day
            calendarAdapter.setSelectedDay(day)
            updateHabits(viewModel.habits.value, viewModel.categories.value)
        }
        
        binding.rvCalendar.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = calendarAdapter
        }
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        val todayDate = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayString = sdf.format(todayDate)

        // Start from 3 days before today to show 7 days total (today in the middle)
        calendar.add(Calendar.DAY_OF_MONTH, -3)

        for (i in 0..6) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val fullDateString = sdf.format(calendar.time)

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
            
            val isToday = fullDateString == todayString

            days.add(CalendarDay(dayOfMonth, dayName, fullDateString, isToday, isToday))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return days
    }

    private fun setupHabits() {
        val habits = mutableListOf<Habit>()
        habitsAdapter = HabitsAdapter(
            habits = habits,
            categories = viewModel.categories.value,
            selectedDate = selectedDay?.fullDate ?: "",
            onHabitClick = { habit ->
                // Navigate to ViewHabitFragment only if selected day is today
                if (selectedDay?.isToday == true) {
                    navigateToViewHabit(habit)
                } else {
                    showError("You can only view details for today")
                }
            },
            onHabitLongClick = { habit ->
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(habit)
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

    private fun showDeleteConfirmationDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteHabit(habit.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun toggleHabitCompletion(habit: Habit) {
        // Restriction: Only allow completion for "Today"
        if (selectedDay?.isToday == false) {
            showError("You can only complete habits for today")
            return
        }

        // Update in Firestore via ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toggleHabitCompletion(habit)
        }
    }

    private fun navigateToViewHabit(habit: Habit) {
        // Start ViewHabitDetailActivity with habitId
        val intent = ViewHabitDetailActivity.newIntent(
            requireContext(),
            habit.id
        )
        startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setupClickListeners() {
        // Navigate to Daily Quote screen when quote card is clicked
        binding.quoteCard.setOnClickListener {
            findNavController().navigate(R.id.nav_daily_quote)
        }

        binding.btnNotification.setOnClickListener {
            notificationViewModel.markAllAsRead()
            showNotificationDropdown(it)
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
