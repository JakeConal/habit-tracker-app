package com.example.habittracker.ui.auth.login

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
import com.example.habittracker.databinding.FragmentLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

import com.example.habittracker.utils.UserPreferences

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        credentialManager = CredentialManager.create(requireContext())
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Success -> {
                    // Save user data to preferences
                    UserPreferences.saveUserId(requireContext(), state.user.id)
                    UserPreferences.saveUserName(requireContext(), state.user.name)
                    UserPreferences.saveUserAvatar(requireContext(), state.user.avatarUrl ?: "")

                    Toast.makeText(requireContext(), "Welcome ${state.user.name}!", Toast.LENGTH_SHORT).show()
                    // Clear back stack to prevent going back to login
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.nav_login, true)
                        .build()
                    findNavController().navigate(R.id.nav_home, null, navOptions)
                    viewModel.resetState()
                }
                is LoginState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                is LoginState.PasswordResetSent -> {
                    Toast.makeText(requireContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                LoginState.Idle -> {
                    // Do nothing
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
            binding.btnContinueGuest.isEnabled = !isLoading
            
            if (isLoading) {
                binding.btnLogin.text = "Signing in..."
            } else {
                binding.btnLogin.text = getString(R.string.login)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.signInWithEmail(email, password)
        }

        binding.btnContinueGuest.setOnClickListener {
            viewModel.signInAsGuest()
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.nav_register)
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.sendPasswordResetEmail(email)
            } else {
                Toast.makeText(requireContext(), "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
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
                viewModel.handleGoogleSignIn(result)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}
