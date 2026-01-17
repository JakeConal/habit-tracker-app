package com.example.habittracker.ui.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentRegisterBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

import com.example.habittracker.utils.UserPreferences

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        credentialManager = CredentialManager.create(requireContext())
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterState.Success -> {
                    // Save user data to preferences
                    UserPreferences.saveUserId(requireContext(), state.user.id)
                    UserPreferences.saveUserName(requireContext(), state.user.name)
                    UserPreferences.saveUserAvatar(requireContext(), state.user.avatarUrl ?: "")

                    Toast.makeText(requireContext(), "Welcome ${state.user.name}!", Toast.LENGTH_SHORT).show()
                    // Clear back stack to prevent going back to login/register
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.nav_login, true)
                        .build()
                    findNavController().navigate(R.id.nav_home, null, navOptions)
                    viewModel.resetState()
                }
                is RegisterState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                RegisterState.Idle -> {
                    // Do nothing
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSignUp.isEnabled = !isLoading
            
            if (isLoading) {
                binding.btnSignUp.text = "Creating account..."
            } else {
                binding.btnSignUp.text = getString(R.string.sign_up)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            if (!binding.cbTerms.isChecked) {
                Toast.makeText(requireContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            
            // Since there's no confirm password field, we pass the same password twice
            viewModel.registerWithEmail(name, email, password, password)
        }

        binding.tvLogIn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvTermsPrivacy.setOnClickListener {
            findNavController().navigate(R.id.action_nav_register_to_nav_terms)
        }
    }

    private fun signUpWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireContext(),
                    request = request
                )
                viewModel.handleGoogleSignUp(result)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Google sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = RegisterFragment()
    }
}
