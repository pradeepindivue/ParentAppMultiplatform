package com.edmik.parentapp.domain.usecase.auth

import com.edmik.parentapp.domain.model.LoginResponse
import com.edmik.parentapp.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(studentId: String, password: String): Result<LoginResponse> {
        return repository.login(studentId, password)
    }
}
