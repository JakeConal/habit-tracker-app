package com.example.habittracker.ui.setting.subsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.data.repository.AuthRepository
import com.example.habittracker.databinding.FragmentResetPasswordBinding
import com.example.habittracker.utils.UserPreferences

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
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

        binding.btnChangePassword.setOnClickListener {
            val oldPass = binding.etCurrentPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmPassword.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), R.string.empty_password_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.changePassword(oldPass, newPass)
        }
    }

    private fun observeViewModel() {
        viewModel.isEmailPasswordUser.observe(viewLifecycleOwner) { isEmailUser ->
            if (isEmailUser) {
                binding.groupEmailFields.visibility = View.VISIBLE
                binding.tvGoogleNotice.visibility = View.GONE
            } else {
                binding.groupEmailFields.visibility = View.GONE
                binding.tvGoogleNotice.visibility = View.VISIBLE
            }
        }

        viewModel.resetPasswordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResetPasswordState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnChangePassword.isEnabled = false
                }
                is ResetPasswordState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), R.string.password_changed_success, Toast.LENGTH_LONG).show()
                    performLogout()
                }
                is ResetPasswordState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnChangePassword.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                ResetPasswordState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnChangePassword.isEnabled = true
                }
            }
        }
    }

    private fun performLogout() {
        // Sign out from Firebase
        AuthRepository.getInstance().signOut()
        
        // Clear local preferences
        UserPreferences.clearUserData(requireContext())
        
        // Navigate to login and clear backstack
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph_main, true)
            .build()
        findNavController().navigate(R.id.nav_login, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
