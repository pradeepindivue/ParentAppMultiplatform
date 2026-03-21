package com.edmik.parentapp.domain.repository

import com.edmik.parentapp.domain.model.LoginResponse


interface AuthRepository {
    suspend fun login(studentId: String, password: String): Result<LoginResponse>
    suspend fun forgotPassword(identifier: String): Result<ForgotPasswordResponse>
}

data class ForgotPasswordResponse(val message: String, val otpSentTo: String)
