package com.edmik.parentapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordStep {
    data object EnterContact : ForgotPasswordStep()
    data object EnterOtp : ForgotPasswordStep()
    data object ResetPassword : ForgotPasswordStep()
    data object Success : ForgotPasswordStep()
}

class ForgotPasswordViewModel : ViewModel() {

    private val _step = MutableStateFlow<ForgotPasswordStep>(ForgotPasswordStep.EnterContact)
    val step: StateFlow<ForgotPasswordStep> = _step.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Input staging — sub-composables push their values here
    val stagedIdentifier = MutableStateFlow("")
    val stagedOtp = MutableStateFlow("")
    val stagedPassword = MutableStateFlow("")
    val stagedConfirmPassword = MutableStateFlow("")

    fun setContactIdentifier(value: String) { stagedIdentifier.value = value }
    fun setOtpInput(value: String) { stagedOtp.value = value }
    fun setNewPassword(password: String, confirm: String) {
        stagedPassword.value = password
        stagedConfirmPassword.value = confirm
    }

    /** Called when the bottom action button is tapped */
    fun onActionButtonClicked() {
        viewModelScope.launch {
            when (_step.value) {
                is ForgotPasswordStep.EnterContact -> sendOtp(stagedIdentifier.value)
                is ForgotPasswordStep.EnterOtp -> verifyOtp(stagedOtp.value)
                is ForgotPasswordStep.ResetPassword -> resetPassword(stagedPassword.value)
                is ForgotPasswordStep.Success -> { /* handled by nav */ }
            }
        }
    }

    fun sendOtp(identifier: String) {
        _isLoading.value = true
        // TODO: Call AuthRepository.forgotPassword(identifier)
        _step.value = ForgotPasswordStep.EnterOtp
        // Reset staged inputs for next step
        stagedOtp.value = ""
        _isLoading.value = false
    }

    fun verifyOtp(otp: String) {
        _isLoading.value = true
        // TODO: Call AuthRepository.verifyOtp(otp)
        _step.value = ForgotPasswordStep.ResetPassword
        stagedPassword.value = ""
        stagedConfirmPassword.value = ""
        _isLoading.value = false
    }

    fun resetPassword(password: String) {
        _isLoading.value = true
        // TODO: Call AuthRepository.resetPassword(password)
        _step.value = ForgotPasswordStep.Success
        _isLoading.value = false
    }
}
