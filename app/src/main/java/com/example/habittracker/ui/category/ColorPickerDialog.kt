package com.example.habittracker.ui.category

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.DialogColorPickerBinding
import com.example.habittracker.databinding.ItemColorOptionBinding

/**
 * ColorPickerDialog - Dialog for selecting category colors
 */
class ColorPickerDialog : DialogFragment() {

    private var _binding: DialogColorPickerBinding? = null
    private val binding get() = _binding!!
    
    private var colors: List<ColorOption> = emptyList()
    private var onColorSelected: ((ColorOption) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvColors.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = ColorAdapter(colors) { color ->
                onColorSelected?.invoke(color)
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
        private const val TAG = "ColorPickerDialog"

        fun show(
            fragmentManager: FragmentManager,
            colors: List<ColorOption>,
            onColorSelected: (ColorOption) -> Unit
        ) {
            val dialog = ColorPickerDialog().apply {
                this.colors = colors
                this.onColorSelected = onColorSelected
            }
            dialog.show(fragmentManager, TAG)
        }
    }
}

/**
 * ColorAdapter - Adapter for displaying color options in a grid
 */
private class ColorAdapter(
    private val colors: List<ColorOption>,
    private val onColorClick: (ColorOption) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int = colors.size

    inner class ColorViewHolder(
        private val binding: ItemColorOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: ColorOption) {
            binding.apply {
                viewColor.setBackgroundResource(color.backgroundRes)
                tvColorName.text = color.name
                
                root.setOnClickListener {
                    onColorClick(color)
                }
            }
        }
    }
}
