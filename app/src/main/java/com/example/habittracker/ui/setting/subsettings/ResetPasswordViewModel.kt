package com.example.habittracker.ui.setting.subsettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

class ResetPasswordViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()

    private val _resetPasswordState = MutableLiveData<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: LiveData<ResetPasswordState> = _resetPasswordState

    private val _isEmailPasswordUser = MutableLiveData<Boolean>()
    val isEmailPasswordUser: LiveData<Boolean> = _isEmailPasswordUser

    init {
        checkUserProvider()
    }

    private fun checkUserProvider() {
        _isEmailPasswordUser.value = authRepository.isEmailPasswordUser()
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (oldPass.isEmpty() || newPass.isEmpty()) {
            _resetPasswordState.value = ResetPasswordState.Error("Password fields cannot be empty")
            return
        }

        _resetPasswordState.value = ResetPasswordState.Loading
        viewModelScope.launch {
            val result = authRepository.changePassword(oldPass, newPass)
            if (result.isSuccess) {
                _resetPasswordState.value = ResetPasswordState.Success
            } else {
                _resetPasswordState.value = ResetPasswordState.Error(result.exceptionOrNull()?.message ?: "An error occurred")
            }
        }
    }

    fun resetState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }
}
