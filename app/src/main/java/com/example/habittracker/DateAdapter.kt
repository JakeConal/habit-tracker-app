package com.example.habittracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemDateBinding

/**
 * Simple data class for date items in the horizontal selector
 */
data class DateItem(
    val dayNumber: String,
    val dayName: String
)

/**
 * RecyclerView adapter for horizontal date selector
 */
class DateAdapter : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val dates = mutableListOf<DateItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    override fun getItemCount(): Int = dates.size

    fun submitList(newDates: List<DateItem>) {
        dates.clear()
        dates.addAll(newDates)
        notifyDataSetChanged()
    }

    class DateViewHolder(
        private val binding: ItemDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dateItem: DateItem) {
            binding.dayNumber.text = dateItem.dayNumber
            binding.dayName.text = dateItem.dayName
        }
    }
}

