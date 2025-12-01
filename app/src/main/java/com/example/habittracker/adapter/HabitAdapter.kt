package com.example.habittracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemHabitBinding
import com.example.habittracker.model.Habit

/**
 * Adapter for displaying habits in a RecyclerView.
 * Follows MVC pattern - only handles view binding, no business logic.
 *
 * @param onCompleteClick Callback invoked when complete button is clicked
 */
class HabitAdapter(
    private val onCompleteClick: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for habit items.
     * Handles binding data to views and click events.
     */
    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            val context = binding.root.context

            binding.habitTitle.text = habit.title

            // Update status text and color based on completion state
            if (habit.isCompleted) {
                binding.habitStatus.text = context.getString(R.string.status_completed)
                binding.habitStatus.setTextColor(ContextCompat.getColor(context, R.color.success))
                binding.completeButton.text = context.getString(R.string.undo_complete)
                binding.habitIcon.setColorFilter(ContextCompat.getColor(context, R.color.success))
            } else {
                binding.habitStatus.text = context.getString(R.string.status_pending)
                binding.habitStatus.setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                binding.completeButton.text = context.getString(R.string.complete_habit)
                binding.habitIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
            }

            // Delegate click handling to the callback
            binding.completeButton.setOnClickListener {
                onCompleteClick(habit)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}
