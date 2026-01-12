package com.example.habittracker.ui.setting.subsettings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()
    
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivAvatar.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.avatarContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        
        binding.tvChangeAvatar.setOnClickListener {
             pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isNotEmpty()) {
                viewModel.updateProfile(name, selectedImageUri, requireContext())
            } else {
                binding.tilName.error = "Name cannot be empty"
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                user?.let {
                    if (selectedImageUri == null) {
                         Glide.with(this@EditProfileFragment)
                            .load(it.avatarUrl)
                            .placeholder(R.drawable.bg_circle_gray) // fallback placeholder
                            .error(R.drawable.bg_circle_gray)
                            .into(binding.ivAvatar)
                    }
                    if (binding.etName.text.isNullOrEmpty()) {
                         binding.etName.setText(it.name)
                    }
                    binding.etEmail.setText(it.email)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is EditProfileUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSave.isEnabled = false
                    }
                    is EditProfileUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is EditProfileUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                    is EditProfileUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
