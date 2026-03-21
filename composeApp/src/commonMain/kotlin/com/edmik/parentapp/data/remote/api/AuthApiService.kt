package com.edmik.parentapp.data.remote.api

import com.edmik.parentapp.data.remote.dto.ApiForgotPasswordResponse
import com.edmik.parentapp.data.remote.dto.ApiForgotPasswordRequest
import com.edmik.parentapp.data.remote.dto.ApiLoginResponse
import com.edmik.parentapp.data.remote.dto.ApiLoginRequest
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApiService(private val httpClientFactory: HttpClientFactory) {
    suspend fun login(request: ApiLoginRequest): ApiLoginResponse {
        return httpClientFactory.client.post("/parent/login") {
            setBody(request)
        }.body()
    }

    suspend fun forgotPassword(request: ApiForgotPasswordRequest): ApiForgotPasswordResponse {
        return httpClientFactory.client.post("/parent/forgot-password") {
            setBody(request)
        }.body()
    }
}
