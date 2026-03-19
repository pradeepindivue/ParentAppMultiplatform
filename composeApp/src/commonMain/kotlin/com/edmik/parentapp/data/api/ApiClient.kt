package com.edmik.parentapp.data.api

import com.edmik.parentapp.data.local.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout

class ApiClient(
    private val tokenManager: TokenManager,
    private val isDebug: Boolean
) {
    private val baseUrl = "https://dev.indivue.in:8080"

    val client = HttpClient {
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 30000
        }

        if (isDebug) {
            install(Logging) {
                level = LogLevel.ALL
            }
        }

        install(Auth) {
            bearer {
                loadTokens {
                    tokenManager.getAccessToken()?.let { access ->
                        tokenManager.getRefreshToken()?.let { refresh ->
                            BearerTokens(access, refresh)
                        }
                    }
                }

                refreshTokens {
                    // Endpoint: /parent/refresh-token
                    // Request : { "refreshToken": refresh }
                    // Response: { "accessToken": "string", "refreshToken": "string" }
                    val currentRefreshToken = tokenManager.getRefreshToken() ?: return@refreshTokens null
                    
                    try {
                        val response = client.post("/parent/refresh-token") {
                            markAsRefreshTokenRequest()
                            setBody(TokenRefreshRequest(currentRefreshToken))
                        }.body<TokenRefreshResponse>()
                        
                        tokenManager.setTokens(response.accessToken, response.refreshToken)
                        BearerTokens(response.accessToken, response.refreshToken)
                    } catch (e: Exception) {
                        tokenManager.clearTokens()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    request.url.build().encodedPath.startsWith("/parent/login") ||
                    request.url.build().encodedPath.startsWith("/parent/forgot-password")
                }
            }
        }
    }
}

@Serializable
data class TokenRefreshRequest(val refreshToken: String)

@Serializable
data class TokenRefreshResponse(val accessToken: String, val refreshToken: String)
