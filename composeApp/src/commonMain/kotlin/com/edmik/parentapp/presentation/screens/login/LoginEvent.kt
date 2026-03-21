package com.edmik.parentapp.presentation.screens.login

sealed class LoginEvent {
    data class OnLoginClicked(val studentId: String, val password: String) : LoginEvent()
}
