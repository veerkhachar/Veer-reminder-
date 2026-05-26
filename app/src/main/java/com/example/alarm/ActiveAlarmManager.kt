package com.example.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ActiveAlarmManager {
    private val _activeTaskId = MutableStateFlow<Int?>(null)
    val activeTaskId: StateFlow<Int?> = _activeTaskId

    private val _activeTaskTitle = MutableStateFlow<String?>(null)
    val activeTaskTitle: StateFlow<String?> = _activeTaskTitle

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val _isEscalatedToRed = MutableStateFlow(false)
    val isEscalatedToRed: StateFlow<Boolean> = _isEscalatedToRed

    fun startAlarm(taskId: Int, title: String) {
        _activeTaskId.value = taskId
        _activeTaskTitle.value = title
        _elapsedSeconds.value = 0
        _isEscalatedToRed.value = false
    }

    fun stopAlarm() {
        _activeTaskId.value = null
        _activeTaskTitle.value = null
        _elapsedSeconds.value = 0
        _isEscalatedToRed.value = false
    }

    fun updateElapsed(seconds: Int) {
        _elapsedSeconds.value = seconds
        if (seconds >= 900) { // 15 minutes
            _isEscalatedToRed.value = true
        }
    }
}
