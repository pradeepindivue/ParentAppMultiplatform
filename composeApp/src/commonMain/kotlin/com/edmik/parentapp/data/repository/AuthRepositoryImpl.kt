package com.edmik.parentapp.data.repository

import com.edmik.parentapp.data.api.AuthService
import com.edmik.parentapp.data.api.ApiLoginRequest
import com.edmik.parentapp.data.api.ApiForgotPasswordRequest
import com.edmik.parentapp.domain.model.LoginResponse
import com.edmik.parentapp.domain.model.StudentSummary
import com.edmik.parentapp.domain.repository.AuthRepository
import com.edmik.parentapp.domain.repository.ForgotPasswordResponse

class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {
    override suspend fun login(studentId: String, password: String): Result<LoginResponse> = runCatching {
        val response = authService.login(ApiLoginRequest(studentId, password))
        LoginResponse(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            student = StudentSummary(
                id = response.student.id,
                name = response.student.name,
                batch = response.student.batch,
                profilePhotoUrl = response.student.profilePhotoUrl
            )
        )
    }

    override suspend fun forgotPassword(identifier: String): Result<ForgotPasswordResponse> = runCatching {
        val response = authService.forgotPassword(ApiForgotPasswordRequest(identifier))
        ForgotPasswordResponse(response.message, response.otpSentTo)
    }
}
