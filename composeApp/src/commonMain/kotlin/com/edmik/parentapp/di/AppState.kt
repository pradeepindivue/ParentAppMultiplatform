package com.edmik.parentapp.di

import com.edmik.parentapp.domain.model.StudentSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppState(
    val currentStudentId: String = "",
    val currentStudentName: String = "",
    val currentStudentBatch: String = "",
    val linkedStudents: List<StudentSummary> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isOffline: Boolean = false
)

class AppStateManager {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun updateState(newState: AppState) {
        _state.value = newState
    }
}
