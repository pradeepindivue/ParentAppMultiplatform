package com.edmik.parentapp.domain.usecase.auth

import com.edmik.parentapp.domain.repository.AuthRepository
import com.edmik.parentapp.domain.repository.ForgotPasswordResponse

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(identifier: String): Result<ForgotPasswordResponse> {
        return repository.forgotPassword(identifier)
    }
}
