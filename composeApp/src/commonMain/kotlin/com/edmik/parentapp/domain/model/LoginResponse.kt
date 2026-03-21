package com.edmik.parentapp.domain.model

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val student: Student
)
