package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Work, Gym, Study, Habit, Custom, etc.
    val priority: String, // LOW, MEDIUM, HIGH, CRITICAL
    val exactTime: Long, // Alarm time in millisecond timestamp
    val deadline: Long, // Deadline time in millisecond timestamp
    val repeatFrequency: String, // ONCE, HOURLY, DAILY, CUSTOM
    val reminderInterval: Int, // Minute intervals to remind
    val proofType: String, // COMPLETED_TAP, SCREENSHOT, PHOTO, TIMER, NOTE, VOICE
    val completed: Boolean = false,
    val missed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completionProofImage: String? = null, // Base64 or local filepath
    val completionNote: String? = null,
    val isImportant: Boolean = false, // If true, triggers accessibility / distraction blocking
    val snoozedCount: Int = 0
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Single row for user metrics
    val xp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val dailyScore: Int = 100, // Starts at 100, drops on laziness
    val completedCount: Int = 0,
    val missedCount: Int = 0,
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // COMPLETED, MISSED, SNOOZED, STREAK_RESET, XP_GAINED, XP_PENALTY
    val taskTitle: String,
    val details: String,
    val xpChange: Int
)
