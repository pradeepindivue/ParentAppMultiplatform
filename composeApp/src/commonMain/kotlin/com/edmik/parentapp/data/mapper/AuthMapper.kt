package com.edmik.parentapp.data.mapper

import com.edmik.parentapp.data.remote.dto.ApiLoginResponse
import com.edmik.parentapp.data.remote.dto.ApiForgotPasswordResponse
import com.edmik.parentapp.domain.model.LoginResponse
import com.edmik.parentapp.domain.model.Student
import com.edmik.parentapp.domain.repository.ForgotPasswordResponse

fun ApiLoginResponse.toDomain() = LoginResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    student = Student(
        id = student.id,
        name = student.name,
        batch = student.batch,
        photoUrl = student.profilePhotoUrl
    )
)


fun ApiForgotPasswordResponse.toDomain() = ForgotPasswordResponse(
    message = message,
    otpSentTo = otpSentTo
)
