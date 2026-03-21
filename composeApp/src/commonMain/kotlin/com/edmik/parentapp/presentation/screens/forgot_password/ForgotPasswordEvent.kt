package com.edmik.parentapp.presentation.screens.forgot_password

sealed class ForgotPasswordEvent {
    data class OnSubmitClicked(val identifier: String) : ForgotPasswordEvent()
}
