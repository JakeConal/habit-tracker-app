package com.example.habittracker.ui.auth.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.resetState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.password_reset_sent_check_inbox),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.resetState()
                    // Navigate back to login
                    findNavController().popBackStack()
                }
                is ForgotPasswordState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                ForgotPasswordState.Idle -> {
                    // Do nothing
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSendReset.isEnabled = !isLoading

            if (isLoading) {
                binding.btnSendReset.text = getString(R.string.sending)
            } else {
                binding.btnSendReset.text = getString(R.string.send_reset_link)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.sendPasswordResetEmail(email)
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ForgotPasswordFragment()
    }
}
