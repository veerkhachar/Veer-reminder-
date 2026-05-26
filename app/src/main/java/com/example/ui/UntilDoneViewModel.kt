package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.alarm.AlarmScheduler
import com.example.data.ActivityLog
import com.example.data.AppDatabase
import com.example.data.Task
import com.example.data.TaskRepository
import com.example.data.UserStats
import com.example.focus.FocusStateHolder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UntilDoneViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    val tasks: StateFlow<List<Task>>
    val completedTasks: StateFlow<List<Task>>
    val missedTasks: StateFlow<List<Task>>
    val userStats: StateFlow<UserStats?>
    val activityLogs: StateFlow<List<ActivityLog>>

    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TaskRepository(db)

        tasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        completedTasks = repository.completedTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        missedTasks = repository.missedTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userStats = repository.userStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        activityLogs = repository.activityLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Restore alarms for all uncompleted active tasks from database flow
        viewModelScope.launch {
            tasks.collectLatest { taskList ->
                val context = getApplication<Application>().applicationContext
                taskList.forEach { task ->
                    if (!task.completed && !task.missed && task.exactTime > System.currentTimeMillis()) {
                        AlarmScheduler.scheduleAlarm(context, task)
                    }
                }
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        exactTime: Long,
        deadline: Long,
        repeatFrequency: String,
        reminderInterval: Int,
        proofType: String,
        isImportant: Boolean
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                category = category,
                priority = priority,
                exactTime = exactTime,
                deadline = deadline,
                repeatFrequency = repeatFrequency,
                reminderInterval = reminderInterval,
                proofType = proofType,
                isImportant = isImportant
            )
            val id = repository.insertTask(task)
            val savedTask = task.copy(id = id.toInt())
            
            // Schedule Alarm
            val context = getApplication<Application>().applicationContext
            AlarmScheduler.scheduleAlarm(context, savedTask)

            // Log Insertion
            repository.insertLog(
                ActivityLog(
                    actionType = "XP_GAINED",
                    taskTitle = title,
                    details = "Task '$title' designed under verification protocol ($proofType). Live tracking active.",
                    xpChange = 0
                )
            )
        }
    }

    fun completeTask(taskId: Int, proofImage: String? = null, note: String? = null) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                // Cancel active alarm
                val context = getApplication<Application>().applicationContext
                AlarmScheduler.cancelAlarm(context, task)
                
                // Complete task in repository (rewards XP, stakes streak)
                repository.completeTask(taskId, proofImage, note)
            }
        }
    }

    fun snoozeTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                // Cancel active alarm and delay
                val context = getApplication<Application>().applicationContext
                AlarmScheduler.cancelAlarm(context, task)
                
                repository.snoozeTask(taskId)
                
                // Fetch the modified task to reschedule new alarm time
                val updated = repository.getTaskById(taskId)
                if (updated != null) {
                    AlarmScheduler.scheduleAlarm(context, updated)
                }
            }
        }
    }

    fun failTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                val context = getApplication<Application>().applicationContext
                AlarmScheduler.cancelAlarm(context, task)
                repository.failTask(taskId)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            AlarmScheduler.cancelAlarm(context, task)
            repository.deleteTask(task)
        }
    }

    fun runAiAnalysis() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val currentStats = userStats.value ?: UserStats(id = 1)
            val currentTasks = tasks.value
            val currentCompleted = completedTasks.value
            val currentMissed = missedTasks.value
            
            val result = GeminiClient.analyzeProcrastination(
                stats = currentStats,
                completedTasks = currentCompleted,
                missedTasks = currentMissed,
                allTasks = currentTasks
            )
            _aiAnalysis.value = result
            _isAnalyzing.value = false
        }
    }

    fun clearHistoryLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
