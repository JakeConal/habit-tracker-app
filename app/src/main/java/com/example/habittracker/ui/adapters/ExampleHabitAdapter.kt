package com.example.habittracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.data.models.ExampleHabit

/**
 * Adapter for displaying a list of habits in a RecyclerView
 */
class ExampleHabitAdapter(
    private val onHabitClick: (ExampleHabit) -> Unit
) : ListAdapter<ExampleHabit, ExampleHabitAdapter.HabitViewHolder>(HabitDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return HabitViewHolder(view, onHabitClick)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class HabitViewHolder(
        itemView: View,
        private val onHabitClick: (ExampleHabit) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val titleText: TextView = itemView.findViewById(android.R.id.text1)
        private val descriptionText: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(habit: ExampleHabit) {
            titleText.text = habit.name
            descriptionText.text = "${habit.frequency} - ${if (habit.isCompleted) "✓ Completed" else "Pending"}"
            itemView.setOnClickListener { onHabitClick(habit) }
        }
    }
    
    class HabitDiffCallback : DiffUtil.ItemCallback<ExampleHabit>() {
        override fun areItemsTheSame(oldItem: ExampleHabit, newItem: ExampleHabit): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ExampleHabit, newItem: ExampleHabit): Boolean {
            return oldItem == newItem
        }
    }
}
