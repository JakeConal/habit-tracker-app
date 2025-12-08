package com.example.habittracker.ui.progress.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemHabitStatBinding

data class HabitStatItem(
    val name: String,
    val score: String,
    val iconRes: Int,
    val iconBgColor: Int,
    val badgeText: String,
    val badgeColor: Int
)

class HabitStatAdapter : RecyclerView.Adapter<HabitStatAdapter.HabitStatViewHolder>() {

    private val items = mutableListOf<HabitStatItem>()

    fun setItems(newItems: List<HabitStatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitStatViewHolder {
        val binding = ItemHabitStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitStatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitStatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class HabitStatViewHolder(private val binding: ItemHabitStatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HabitStatItem) {
            binding.tvHabitName.text = item.name
            binding.tvHabitScore.text = item.score
            binding.ivHabitIcon.setImageResource(item.iconRes)
            binding.iconCard.setCardBackgroundColor(item.iconBgColor)
            binding.tvBadge.text = item.badgeText
            binding.badgeCard.setCardBackgroundColor(item.badgeColor)
        }
    }
}

