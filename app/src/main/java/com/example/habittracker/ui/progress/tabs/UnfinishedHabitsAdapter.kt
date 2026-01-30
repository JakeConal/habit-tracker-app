package com.example.habittracker.ui.progress.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemUnfinishedHabitBinding
import com.example.habittracker.ui.progress.UnfinishedHabit

class UnfinishedHabitsAdapter(
    private val onHabitClick: (String) -> Unit
) : ListAdapter<UnfinishedHabit, UnfinishedHabitsAdapter.HabitViewHolder>(HabitDiffCallback()) {

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

        fun bind(habit: UnfinishedHabit) {
            binding.tvHabitName.text = habit.name
            binding.tvHabitFrequency.text = habit.frequency

            // Set icon image and background
            binding.habitIcon.setImageResource(habit.iconRes)
            binding.habitIconCard.setBackgroundResource(habit.iconBgRes)

            binding.root.setOnClickListener {
                onHabitClick(habit.id)
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<UnfinishedHabit>() {
        override fun areItemsTheSame(oldItem: UnfinishedHabit, newItem: UnfinishedHabit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UnfinishedHabit, newItem: UnfinishedHabit): Boolean {
            return oldItem == newItem
        }
    }
}
