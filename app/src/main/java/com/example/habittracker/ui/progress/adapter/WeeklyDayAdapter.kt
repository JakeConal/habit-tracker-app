package com.example.habittracker.ui.progress.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemWeeklyDayBinding

data class WeeklyDayData(
    val dayNumber: String,
    val dayName: String,
    val isCompleted: Boolean,
    val isMissed: Boolean,
    val isUpcoming: Boolean
)

class WeeklyDayAdapter : RecyclerView.Adapter<WeeklyDayAdapter.WeeklyDayViewHolder>() {

    private val days = mutableListOf<WeeklyDayData>()

    fun setDays(newDays: List<WeeklyDayData>) {
        days.clear()
        days.addAll(newDays)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyDayViewHolder {
        val binding = ItemWeeklyDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeeklyDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeeklyDayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    class WeeklyDayViewHolder(private val binding: ItemWeeklyDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: WeeklyDayData) {
            binding.tvDayNumber.text = day.dayNumber
            binding.tvDayName.text = day.dayName

            val context = binding.root.context

            when {
                day.isCompleted -> {
                    // Green background for completed
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.calendar_day_completed_bg))
                    binding.dayCard.strokeColor = ContextCompat.getColor(context, R.color.calendar_day_completed_stroke)
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
                day.isMissed -> {
                    // Yellow/orange background for missed
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.calendar_day_missed_bg))
                    binding.dayCard.strokeColor = ContextCompat.getColor(context, R.color.calendar_day_missed_stroke)
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
                day.isUpcoming -> {
                    // Gray background for upcoming/future days
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.calendar_day_upcoming_bg))
                    binding.dayCard.strokeColor = ContextCompat.getColor(context, R.color.calendar_day_upcoming_stroke)
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.secondary_gray))
                }
            }
        }
    }
}
