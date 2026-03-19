package com.edmik.parentapp.domain.model

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val student: StudentSummary
)

data class StudentSummary(
    val id: String,
    val name: String,
    val batch: String,
    val profilePhotoUrl: String?
)

data class LoginRequest(
    val studentId: String,
    val password: String
)
