package com.example.habittracker.ui.habit.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.data.model.Habit
import com.example.habittracker.util.formatFrequency

/**
 * Adapter for displaying a list of habits in a RecyclerView
 */
class HabitAdapter(
    private val onHabitClick: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {
    
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
        private val onHabitClick: (Habit) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val titleText: TextView = itemView.findViewById(android.R.id.text1)
        private val descriptionText: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(habit: Habit) {
            titleText.text = habit.name
            descriptionText.text = "${habit.frequency.formatFrequency()} - ${if (habit.isCompleted) "✓ Completed" else "Pending"}"
            itemView.setOnClickListener { onHabitClick(habit) }
        }
    }
    
    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}

