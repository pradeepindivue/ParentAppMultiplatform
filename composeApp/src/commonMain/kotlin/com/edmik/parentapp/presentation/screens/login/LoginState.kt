package com.edmik.parentapp.presentation.screens.login

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
    data class RateLimited(val secondsLeft: Int) : LoginState()
}
