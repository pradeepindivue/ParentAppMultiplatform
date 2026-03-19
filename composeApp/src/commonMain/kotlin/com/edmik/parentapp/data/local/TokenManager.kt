package com.edmik.parentapp.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get

class TokenManager(private val settings: Settings) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_HAS_LOGGED_IN = "has_logged_in_before"
    }

    fun getAccessToken(): String? = settings.getStringOrNull(KEY_ACCESS_TOKEN)

    fun getRefreshToken(): String? = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    fun setTokens(access: String, refresh: String) {
        settings.putString(KEY_ACCESS_TOKEN, access)
        settings.putString(KEY_REFRESH_TOKEN, refresh)
        settings.putBoolean(KEY_HAS_LOGGED_IN, true)
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    fun hasLoggedInBefore(): Boolean = settings.getBoolean(KEY_HAS_LOGGED_IN, false)
}
