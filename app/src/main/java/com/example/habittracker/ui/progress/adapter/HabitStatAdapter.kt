package com.example.habittracker.ui.progress.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemHabitStatBinding
import com.example.habittracker.ui.progress.DayStatus

data class HabitStatItem(
    val habitId: String = "",
    val name: String,
    val score: String,
    val iconRes: Int,
    val iconBgRes: Int,
    val badgeText: String,
    val badgeBgRes: Int,
    val weeklyDays: List<Pair<String, DayStatus>> = emptyList()
)

class HabitStatAdapter(
    private val onHabitClick: (String) -> Unit = {}
) : RecyclerView.Adapter<HabitStatAdapter.HabitStatViewHolder>() {

    private val items = mutableListOf<HabitStatItem>()

    fun setItems(newItems: List<HabitStatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitStatViewHolder {
        val binding = ItemHabitStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitStatViewHolder(binding, onHabitClick)
    }

    override fun onBindViewHolder(holder: HabitStatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class HabitStatViewHolder(
        private val binding: ItemHabitStatBinding,
        private val onHabitClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var weeklyDayAdapter: WeeklyDayAdapter

        fun bind(item: HabitStatItem) {
            binding.tvHabitName.text = item.name
            binding.tvHabitScore.text = item.score
            binding.ivHabitIcon.setImageResource(item.iconRes)
            binding.iconCard.setBackgroundResource(item.iconBgRes)

            // Set click listener on the root view
            binding.root.setOnClickListener {
                onHabitClick(item.habitId)
            }

            // Show badge only for Daily habits
            if (item.badgeText.contains("Daily", ignoreCase = true)) {
                binding.badgeCard.visibility = android.view.View.VISIBLE
                binding.tvBadge.text = binding.root.context.getString(R.string.daily)
                binding.badgeCard.setBackgroundResource(item.badgeBgRes)
            } else {
                binding.badgeCard.visibility = android.view.View.GONE
            }

            // Setup weekly days RecyclerView
            if (!::weeklyDayAdapter.isInitialized) {
                weeklyDayAdapter = WeeklyDayAdapter()
                binding.rvWeeklyDays.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = weeklyDayAdapter
                }
            }

            // Convert weeklyDays to WeeklyDayData
            val weeklyDayData = item.weeklyDays.mapIndexed { index, (dateStr, status) ->
                val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val dayNumber = dateStr.split("-").last()
                
                WeeklyDayData(
                    dayNumber = dayNumber,
                    dayName = dayNames[index],
                    isCompleted = status == DayStatus.COMPLETED,
                    isMissed = status == DayStatus.MISSED,
                    isUpcoming = status == DayStatus.UPCOMING
                )
            }
            
            weeklyDayAdapter.setDays(weeklyDayData)
        }
    }
}
