package com.edmik.parentapp.presentation.screens.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edmik.parentapp.domain.usecase.auth.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.OnSubmitClicked -> forgotPassword(event.identifier)
        }
    }

    private fun forgotPassword(identifier: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordState.Loading
            forgotPasswordUseCase(identifier)
                .onSuccess { response ->
                    _uiState.value = ForgotPasswordState.Success(response.message)
                }
                .onFailure { error ->
                    _uiState.value = ForgotPasswordState.Error(error.message ?: "Reset password failed")
                }
        }
    }
}
