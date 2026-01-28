package com.example.habittracker.ui.setting.subsettings

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.User
import com.example.habittracker.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        setupGenderDropdown()
        setupDobPicker()
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
                val gender = genderValueFromUi(binding.etGender.text?.toString())
                val dob = binding.etDob.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() }

                viewModel.updateProfile(
                    name = name,
                    imageUri = selectedImageUri,
                    gender = gender,
                    dateOfBirth = dob,
                    context = requireContext()
                )
            } else {
                binding.tilName.error = "Name cannot be empty"
            }
        }
    }

    private fun setupGenderDropdown() {
        val items = listOf(
            getString(R.string.gender_prefer_not_to_say),
            getString(R.string.gender_male),
            getString(R.string.gender_female),
            getString(R.string.gender_other)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        binding.etGender.setAdapter(adapter)
    }

    private fun setupDobPicker() {
        // Allow clicking on both input and end icon
        binding.etDob.setOnClickListener { showDobPicker() }
        binding.tilDob.setEndIconOnClickListener { showDobPicker() }
    }

    private fun showDobPicker() {
        val existing = binding.etDob.text?.toString()?.trim().orEmpty()
        val initialDate = parseIsoDate(existing) ?: LocalDate.now().minusYears(20)

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selected = LocalDate.of(year, month + 1, dayOfMonth)
                binding.etDob.setText(selected.format(DateTimeFormatter.ISO_LOCAL_DATE))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        )
        dialog.show()
    }

    private fun parseIsoDate(value: String): LocalDate? {
        return try {
            if (value.isBlank()) null else LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
    }

    private fun genderValueFromUi(text: String?): String? {
        val t = text?.trim().orEmpty()
        if (t.isEmpty()) return null
        return when (t) {
            getString(R.string.gender_male) -> User.GENDER_MALE
            getString(R.string.gender_female) -> User.GENDER_FEMALE
            getString(R.string.gender_other) -> User.GENDER_OTHER
            getString(R.string.gender_prefer_not_to_say) -> User.GENDER_PREFER_NOT_TO_SAY
            else -> null
        }
    }

    private fun genderUiFromValue(value: String?): String {
        return when (value) {
            User.GENDER_MALE -> getString(R.string.gender_male)
            User.GENDER_FEMALE -> getString(R.string.gender_female)
            User.GENDER_OTHER -> getString(R.string.gender_other)
            User.GENDER_PREFER_NOT_TO_SAY -> getString(R.string.gender_prefer_not_to_say)
            else -> getString(R.string.gender_prefer_not_to_say)
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

                    // New fields
                    if (binding.etGender.text.isNullOrEmpty()) {
                        binding.etGender.setText(genderUiFromValue(it.gender), false)
                    }
                    if (binding.etDob.text.isNullOrEmpty()) {
                        binding.etDob.setText(it.dateOfBirth.orEmpty())
                    }
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
