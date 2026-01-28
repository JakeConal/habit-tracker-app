package com.example.habittracker.ui.progress.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemHabitStatBinding
import com.example.habittracker.databinding.ItemHabitStatLegendBinding
import com.example.habittracker.ui.progress.DayStatus

data class HabitStatItem(
    val habitId: String = "",
    val name: String,
    val iconRes: Int,
    val iconBgRes: Int,
    val badgeText: String,
    val badgeBgRes: Int,
    val weeklyDays: List<Pair<String, DayStatus>> = emptyList()
)

class HabitStatAdapter(
    private val onHabitClick: (String) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HabitStatItem>()

    companion object {
        private const val TYPE_LEGEND = 0
        private const val TYPE_HABIT = 1
    }

    fun setItems(newItems: List<HabitStatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_LEGEND else TYPE_HABIT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_LEGEND) {
            val binding = ItemHabitStatLegendBinding.inflate(inflater, parent, false)
            LegendViewHolder(binding)
        } else {
            val binding = ItemHabitStatBinding.inflate(inflater, parent, false)
            HabitStatViewHolder(binding, onHabitClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HabitStatViewHolder) {
            holder.bind(items[position - 1])
        }
    }

    override fun getItemCount(): Int = if (items.isEmpty()) 0 else items.size + 1

    class LegendViewHolder(binding: ItemHabitStatLegendBinding) : RecyclerView.ViewHolder(binding.root)

    class HabitStatViewHolder(
        private val binding: ItemHabitStatBinding,
        private val onHabitClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var weeklyDayAdapter: WeeklyDayAdapter

        fun bind(item: HabitStatItem) {
            binding.tvHabitName.text = item.name
            binding.ivHabitIcon.setImageResource(item.iconRes)
            binding.iconCard.setBackgroundResource(item.iconBgRes)

            // Set click listener on the root view
            binding.root.setOnClickListener {
                onHabitClick(item.habitId)
            }

            // Show badge for all habits
            binding.badgeCard.visibility = android.view.View.VISIBLE
            binding.tvBadge.text = if (item.badgeText.contains("Daily", ignoreCase = true)) {
                binding.root.context.getString(R.string.daily)
            } else {
                item.badgeText
            }
            binding.badgeCard.setBackgroundResource(item.badgeBgRes)

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
                    status = status
                )
            }

            weeklyDayAdapter.setDays(weeklyDayData)
        }
    }
}
