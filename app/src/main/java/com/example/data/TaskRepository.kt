package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max

class TaskRepository(private val db: AppDatabase) {
    private val taskDao = db.taskDao()
    private val userStatsDao = db.userStatsDao()
    private val activityLogDao = db.activityLogDao()

    val allTasks: Flow<List<Task>> = taskDao.getAllTasksFlow()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasksFlow()
    val missedTasks: Flow<List<Task>> = taskDao.getMissedTasksFlow()
    val userStats: Flow<UserStats?> = userStatsDao.getUserStatsFlow()
    val activityLogs: Flow<List<ActivityLog>> = activityLogDao.getAllLogsFlow()

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Int) = taskDao.deleteTaskById(id)

    // Complete a task: add XP, check level progress, update streak
    suspend fun completeTask(taskId: Int, proofImage: String? = null, note: String? = null) {
        val task = taskDao.getTaskById(taskId) ?: return
        if (task.completed) return

        // Update task state
        val updatedTask = task.copy(
            completed = true,
            missed = false,
            completionProofImage = proofImage,
            completionNote = note
        )
        taskDao.updateTask(updatedTask)

        // Fetch and calculate new stats
        val stats = userStatsDao.getUserStats() ?: UserStats(id = 1)
        val xpGain = when (task.priority) {
            "LOW" -> 15
            "MEDIUM" -> 25
            "HIGH" -> 40
            "CRITICAL" -> 60
            else -> 20
        }

        var newXp = stats.xp + xpGain
        var newLevel = stats.level
        var xpRequired = newLevel * 150

        val levelUps = mutableListOf<Int>()
        while (newXp >= xpRequired) {
            newXp -= xpRequired
            newLevel++
            xpRequired = newLevel * 150
            levelUps.add(newLevel)
        }

        val newStreak = stats.currentStreak + 1
        val newMaxStreak = max(stats.maxStreak, newStreak)
        val newDailyScore = minOf(100, stats.dailyScore + 10)

        userStatsDao.insertOrUpdateStats(
            stats.copy(
                xp = newXp,
                level = newLevel,
                currentStreak = newStreak,
                maxStreak = newMaxStreak,
                dailyScore = newDailyScore,
                completedCount = stats.completedCount + 1,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        )

        // Log completion action
        activityLogDao.insertLog(
            ActivityLog(
                actionType = "COMPLETED",
                taskTitle = task.title,
                details = "Completed task proof submitted (${task.proofType}). Note: ${note ?: "None"}",
                xpChange = xpGain
            )
        )

        // Log Level Ups
        for (lvl in levelUps) {
            activityLogDao.insertLog(
                ActivityLog(
                    actionType = "LEVEL_UP",
                    taskTitle = "Level $lvl Reached!",
                    details = "Congratulations! You promoted to Level $lvl.",
                    xpChange = 0
                )
            )
        }
    }

    // Fail task: punish user, break streak, deduct XP
    suspend fun failTask(taskId: Int, reason: String = "Alarm ignored / Procrastination") {
        val task = taskDao.getTaskById(taskId) ?: return
        if (task.completed || task.missed) return

        val updatedTask = task.copy(missed = true)
        taskDao.updateTask(updatedTask)

        val stats = userStatsDao.getUserStats() ?: UserStats(id = 1)
        val xpDeduction = when (task.priority) {
            "LOW" -> 10
            "MEDIUM" -> 15
            "HIGH" -> 25
            "CRITICAL" -> 40
            else -> 15
        }

        val newXp = max(0, stats.xp - xpDeduction)
        val oldStreak = stats.currentStreak
        val newDailyScore = max(0, stats.dailyScore - 20)

        userStatsDao.insertOrUpdateStats(
            stats.copy(
                xp = newXp,
                currentStreak = 0,
                dailyScore = newDailyScore,
                missedCount = stats.missedCount + 1,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        )

        // Log penalty action
        activityLogDao.insertLog(
            ActivityLog(
                actionType = "XP_PENALTY",
                taskTitle = task.title,
                details = "Missed task: $reason. Lost old streak of $oldStreak days.",
                xpChange = -xpDeduction
            )
        )
    }

    // Snooze a task - smaller penalty to XP and dailyScore to discourage repeated delays
    suspend fun snoozeTask(taskId: Int) {
        val task = taskDao.getTaskById(taskId) ?: return
        if (task.completed) return

        val updatedTask = task.copy(
            snoozedCount = task.snoozedCount + 1,
            exactTime = System.currentTimeMillis() + (5 * 60 * 1000) // Delay by 5 minutes
        )
        taskDao.updateTask(updatedTask)

        val stats = userStatsDao.getUserStats() ?: UserStats(id = 1)
        
        // Repeated snoozing lowers XP by 5 and score by 3
        val penalty = 5
        var newXp = max(0, stats.xp - penalty)
        val newDailyScore = max(0, stats.dailyScore - 5)

        userStatsDao.insertOrUpdateStats(
            stats.copy(
                xp = newXp,
                dailyScore = newDailyScore,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        )

        activityLogDao.insertLog(
            ActivityLog(
                actionType = "SNOOZED",
                taskTitle = task.title,
                details = "Task snoozed (Count: ${updatedTask.snoozedCount}). Deducted $penalty XP.",
                xpChange = -penalty
            )
        )
    }

    suspend fun insertLog(log: ActivityLog) {
        activityLogDao.insertLog(log)
    }
    
    suspend fun clearLogs() {
        activityLogDao.clearLogs()
    }
}
