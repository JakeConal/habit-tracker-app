package com.example.habittracker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemCalendarDayBinding

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition = days.indexOfFirst { it.isSelected }

    inner class CalendarViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay, isSelected: Boolean) {
            binding.apply {
                tvDayNumber.text = day.dayNumber.toString()
                tvDayName.text = day.dayName

                // Update background and text colors based on selection
                if (isSelected) {
                    // Selected state - primary blue background with white text
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.primary_blue)
                    )
                    root.cardElevation = itemView.context.resources.getDimension(R.dimen.elevation_card)
                    tvDayNumber.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.white)
                    )
                    tvDayName.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.white)
                    )
                } else {
                    // Unselected state - pure white background with original text colors
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.white)
                    )
                    root.cardElevation = itemView.context.resources.getDimension(R.dimen.no_elevation)
                    tvDayNumber.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.primary_blue)
                    )
                    tvDayName.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                }

                root.setOnClickListener {
                    onDayClick(day)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = days.size

    fun setSelectedDay(day: CalendarDay) {
        val newPosition = days.indexOf(day)
        if (newPosition != -1 && newPosition != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}
