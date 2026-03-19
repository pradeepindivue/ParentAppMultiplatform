package com.edmik.parentapp.data.api

import com.edmik.parentapp.domain.model.LoginRequest
import com.edmik.parentapp.domain.model.LoginResponse
import com.edmik.parentapp.domain.repository.ForgotPasswordResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class ApiLoginRequest(val studentId: String, val password: String)

@Serializable
data class ApiLoginResponse(val accessToken: String, val refreshToken: String, val student: ApiStudentSummary)

@Serializable
data class ApiStudentSummary(val id: String, val name: String, val batch: String, val profilePhotoUrl: String?)

@Serializable
data class ApiForgotPasswordRequest(val identifier: String)

@Serializable
data class ApiForgotPasswordResponse(val message: String, val otpSentTo: String)

class AuthService(private val apiClient: ApiClient) {
    suspend fun login(request: ApiLoginRequest): ApiLoginResponse {
        return apiClient.client.post("/parent/login") {
            setBody(request)
        }.body()
    }

    suspend fun forgotPassword(request: ApiForgotPasswordRequest): ApiForgotPasswordResponse {
        return apiClient.client.post("/parent/forgot-password") {
            setBody(request)
        }.body()
    }
}
