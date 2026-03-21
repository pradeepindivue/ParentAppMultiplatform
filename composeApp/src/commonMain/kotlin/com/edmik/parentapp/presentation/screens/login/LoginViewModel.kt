package com.edmik.parentapp.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edmik.parentapp.data.local.database.TokenManager
import com.edmik.parentapp.domain.usecase.auth.LoginUseCase
import com.edmik.parentapp.presentation.app.AppStateManager
import com.edmik.parentapp.presentation.app.AppState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager,
    private val appStateManager: AppStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private var failureCount = 0
    private var lastFailureTime: Instant? = null
    private var countdownJob: Job? = null

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnLoginClicked -> login(event.studentId, event.password)
        }
    }

    private fun login(studentId: String, password: String) {
        if (isRateLimited()) return

        viewModelScope.launch {
            _uiState.value = LoginState.Loading
            loginUseCase(studentId, password)
                .onSuccess { response ->
                    tokenManager.setTokens(response.accessToken, response.refreshToken)
                    appStateManager.updateState(
                        AppState(
                            currentStudentId = response.student.id,
                            currentStudentName = response.student.name,
                            currentStudentBatch = response.student.batch
                        )
                    )
                    _uiState.value = LoginState.Success
                    failureCount = 0
                }
                .onFailure { error ->
                    failureCount++
                    lastFailureTime = Clock.System.now()
                    
                    if (failureCount >= 5) {
                        startRateLimitCountdown(30)
                    } else {
                        _uiState.value = LoginState.Error(error.message ?: "Authentication failed")
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
                _uiState.value = LoginState.RateLimited(i)
                delay(1000)
            }
            _uiState.value = LoginState.Idle
            failureCount = 0
        }
    }

    fun hasLoggedInBefore(): Boolean = tokenManager.hasLoggedInBefore()
}
