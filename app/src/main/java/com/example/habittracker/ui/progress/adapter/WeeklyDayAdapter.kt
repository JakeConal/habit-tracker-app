package com.example.habittracker.ui.progress.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemWeeklyDayBinding
import com.example.habittracker.ui.progress.DayStatus

data class WeeklyDayData(
    val dayNumber: String,
    val dayName: String,
    val status: DayStatus
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

            when (day.status) {
                DayStatus.COMPLETED -> {
                    // Green background for completed
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.habit_completed))
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.secondary_gray))
                }
                DayStatus.MISSED -> {
                    // Red background for missed
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.destructive_red_light))
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.secondary_gray))
                }
                DayStatus.NOT_HABIT_DAY -> {
                    // Dim white/gray background for non-habit days (trắng xám mờ)
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_100))
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.gray_400))
                    binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.gray_300))
                }
                DayStatus.HABIT_DAY -> {
                    // White background for habit days
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                    binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.secondary_gray))
                }
                DayStatus.TODAY_PENDING -> {
                    // Orange background for today's pending habit (màu cam)
                    binding.dayCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.icon_bg_orange))
                    binding.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.secondary_gray))
                }
            }
        }
    }
}
