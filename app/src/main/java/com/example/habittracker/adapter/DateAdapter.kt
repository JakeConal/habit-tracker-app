package com.example.habittracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemDateBinding
import com.example.habittracker.model.DateItem

/**
 * Adapter for displaying dates in a horizontal RecyclerView.
 * 
 * @param onDateClick Callback invoked when a date is selected
 */
class DateAdapter(
    private val onDateClick: (DateItem) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var dates: List<DateItem> = emptyList()
    private var selectedPosition: Int = -1

    fun submitList(items: List<DateItem>) {
        dates = items
        // Find the initially selected item
        selectedPosition = dates.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        if (oldPosition != -1) notifyItemChanged(oldPosition)
        if (position != -1) notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = dates.size

    /**
     * ViewHolder for date items.
     */
    inner class DateViewHolder(
        private val binding: ItemDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onDateClick(dates[position])
                }
            }
        }

        fun bind(dateItem: DateItem, isSelected: Boolean) {
            val context = binding.root.context

            binding.dayOfWeek.text = dateItem.dayOfWeek
            binding.dayNumber.text = dateItem.dayNumber

            // Update visual state based on selection
            binding.root.isSelected = isSelected
            if (isSelected) {
                binding.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.onPrimary))
                binding.dayNumber.setTextColor(ContextCompat.getColor(context, R.color.onPrimary))
            } else {
                binding.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                binding.dayNumber.setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
            }
        }
    }
}
