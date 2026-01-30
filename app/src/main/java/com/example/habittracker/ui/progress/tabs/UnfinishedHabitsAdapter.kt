package com.example.habittracker.ui.progress.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.data.model.Habit
import com.example.habittracker.databinding.ItemUnfinishedHabitBinding

class UnfinishedHabitsAdapter(
    private val onHabitClick: (Habit) -> Unit
) : ListAdapter<Habit, UnfinishedHabitsAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemUnfinishedHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemUnfinishedHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.tvHabitName.text = habit.name
            binding.tvHabitFrequency.text = if (habit.frequency.isNotEmpty()) {
                habit.frequency.joinToString(", ")
            } else {
                "Daily"
            }

            // Set icon background color based on category or default
            binding.habitIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(
                binding.root.context.getColor(com.example.habittracker.R.color.accent_blue)
            )

            binding.root.setOnClickListener {
                onHabitClick(habit)
            }
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
