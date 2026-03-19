package com.edmik.parentapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edmik.parentapp.data.local.TokenManager
import com.edmik.parentapp.di.AppStateManager
import com.edmik.parentapp.di.AppState
import com.edmik.parentapp.domain.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class RateLimited(val secondsLeft: Int) : LoginUiState()
}

class LoginViewModel(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager,
    private val appStateManager: AppStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var failureCount = 0
    private var lastFailureTime: Instant? = null
    private var countdownJob: Job? = null

    fun login(studentId: String, password: String) {
        if (isRateLimited()) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            repository.login(studentId, password)
                .onSuccess { response ->
                    tokenManager.setTokens(response.accessToken, response.refreshToken)
                    appStateManager.updateState(
                        AppState(
                            currentStudentId = response.student.id,
                            currentStudentName = response.student.name,
                            currentStudentBatch = response.student.batch
                        )
                    )
                    _uiState.value = LoginUiState.Success
                    failureCount = 0
                }
                .onFailure { error ->
                    failureCount++
                    lastFailureTime = Clock.System.now()
                    
                    if (failureCount >= 5) {
                        startRateLimitCountdown(30)
                    } else {
                        _uiState.value = LoginUiState.Error(error.message ?: "Authentication failed")
                    }
                }
        }
    }

    private fun isRateLimited(): Boolean {
        lastFailureTime?.let { lastTime ->
            val now = Clock.System.now()
            if (failureCount >= 5 && now.epochSeconds - lastTime.epochSeconds < 30) {
                return true
            }
        }
        return false
    }

    private fun startRateLimitCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 0) {
                _uiState.value = LoginUiState.RateLimited(i)
                delay(1000)
            }
            _uiState.value = LoginUiState.Idle
            failureCount = 0
        }
    }

    fun hasLoggedInBefore(): Boolean = tokenManager.hasLoggedInBefore()
}
