package com.example.habittracker.ui.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R

class SettingsMenuAdapter(
    private val onItemClick: (SettingMenuItem) -> Unit
) : ListAdapter<SettingMenuItem, SettingsMenuAdapter.SettingMenuViewHolder>(SettingMenuDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setting_menu, parent, false)
        return SettingMenuViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: SettingMenuViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    class SettingMenuViewHolder(
        itemView: View,
        private val onItemClick: (SettingMenuItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
        private val menuTitle: TextView = itemView.findViewById(R.id.menuTitle)
        private val divider: View = itemView.findViewById(R.id.divider)
        private val container: View = itemView.findViewById(R.id.menuItemContainer)

        fun bind(item: SettingMenuItem, isLastItem: Boolean) {
            menuIcon.setImageResource(item.iconRes)
            menuTitle.setText(item.titleRes)
            
            // Hide divider for last item
            divider.visibility = if (isLastItem) View.GONE else View.VISIBLE

            container.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private class SettingMenuDiffCallback : DiffUtil.ItemCallback<SettingMenuItem>() {
        override fun areItemsTheSame(oldItem: SettingMenuItem, newItem: SettingMenuItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SettingMenuItem, newItem: SettingMenuItem): Boolean {
            return oldItem == newItem
        }
    }
}
