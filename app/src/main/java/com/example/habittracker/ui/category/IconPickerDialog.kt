package com.example.habittracker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.DialogIconPickerBinding
import com.example.habittracker.databinding.ItemIconOptionBinding

/**
 * IconPickerDialog - Dialog for selecting category icons
 */
class IconPickerDialog : DialogFragment() {

    private var _binding: DialogIconPickerBinding? = null
    private val binding get() = _binding!!
    
    private var icons: List<IconOption> = emptyList()
    private var onIconSelected: ((IconOption) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIconPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = IconAdapter(icons) { icon ->
                onIconSelected?.invoke(icon)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "IconPickerDialog"

        fun show(
            fragmentManager: FragmentManager,
            icons: List<IconOption>,
            onIconSelected: (IconOption) -> Unit
        ) {
            val dialog = IconPickerDialog().apply {
                this.icons = icons
                this.onIconSelected = onIconSelected
            }
            dialog.show(fragmentManager, TAG)
        }
    }
}

/**
 * IconAdapter - Adapter for displaying icon options in a grid
 */
private class IconAdapter(
    private val icons: List<IconOption>,
    private val onIconClick: (IconOption) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position])
    }

    override fun getItemCount(): Int = icons.size

    inner class IconViewHolder(
        private val binding: ItemIconOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(icon: IconOption) {
            binding.apply {
                ivIcon.setImageResource(icon.iconRes)
                tvIconName.text = icon.name
                
                root.setOnClickListener {
                    onIconClick(icon)
                }
            }
        }
    }
}
