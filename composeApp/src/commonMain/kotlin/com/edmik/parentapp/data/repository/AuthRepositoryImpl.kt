package com.edmik.parentapp.data.repository

import com.edmik.parentapp.data.remote.api.AuthApiService
import com.edmik.parentapp.data.remote.dto.ApiLoginRequest
import com.edmik.parentapp.data.remote.dto.ApiForgotPasswordRequest
import com.edmik.parentapp.data.mapper.toDomain
import com.edmik.parentapp.domain.model.LoginResponse
import com.edmik.parentapp.domain.repository.AuthRepository
import com.edmik.parentapp.domain.repository.ForgotPasswordResponse

class AuthRepositoryImpl(private val authService: AuthApiService) : AuthRepository {
    override suspend fun login(studentId: String, password: String): Result<LoginResponse> = runCatching {
        authService.login(ApiLoginRequest(studentId, password)).toDomain()
    }

    override suspend fun forgotPassword(identifier: String): Result<ForgotPasswordResponse> = runCatching {
        authService.forgotPassword(ApiForgotPasswordRequest(identifier)).toDomain()
    }
}

