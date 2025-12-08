package com.example.habittracker.ui.progress.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemCalendarMonthDayBinding

data class CalendarDay(
    val day: String,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val backgroundColor: Int? = null
)

class CalendarDayAdapter : RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder>() {

    private val items = mutableListOf<CalendarDay>()

    fun setItems(newItems: List<CalendarDay>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val binding = ItemCalendarMonthDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CalendarDayViewHolder(private val binding: ItemCalendarMonthDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CalendarDay) {
            binding.tvDay.text = item.day

            if (!item.isCurrentMonth) {
                binding.tvDay.alpha = 0.3f
                binding.tvDay.setTextColor(binding.root.context.getColor(R.color.secondary_gray))
                binding.dayContainer.setBackgroundResource(R.drawable.bg_calendar_day_unselected)
            } else if (item.backgroundColor != null) {
                binding.tvDay.alpha = 1f
                binding.tvDay.setTextColor(binding.root.context.getColor(R.color.primary_blue))
                    if (item.isSelected)
                        binding.root.context.getColor(R.color.white)
                    else
                        binding.root.context.getColor(R.color.primary_blue)
                binding.dayContainer.setBackgroundResource(R.drawable.bg_calendar_day_unselected)
                binding.dayContainer.setBackgroundResource(item.backgroundColor)
            } else {
                binding.tvDay.alpha = 1f
                binding.tvDay.setTextColor(binding.root.context.getColor(R.color.primary_blue))
                binding.dayContainer.setBackgroundColor(binding.root.context.getColor(android.R.color.transparent))
            }
        }
    }
}

