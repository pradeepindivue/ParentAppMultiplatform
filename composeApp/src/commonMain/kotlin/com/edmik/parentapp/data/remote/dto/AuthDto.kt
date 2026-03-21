package com.edmik.parentapp.data.remote.dto

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

@Serializable
data class TokenRefreshRequest(val refreshToken: String)

@Serializable
data class TokenRefreshResponse(val accessToken: String, val refreshToken: String)
