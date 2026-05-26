package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.Task
import com.example.data.UserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeProcrastination(
        stats: UserStats,
        completedTasks: List<Task>,
        missedTasks: List<Task>,
        allTasks: List<Task>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "⚙️ **AI Offline**: Configure your Google AI Studio Gemini API key in the Secrets Panel to unlock analytical feedback."
        }

        val prompt = buildAnalysisPrompt(stats, completedTasks, missedTasks, allTasks)

        try {
            // Build raw JSON request body to avoid any serialization mismatches
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBody")
                return@withContext "❌ **API Error ${response.code}**: Failed to analyze patterns."
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            
            firstPart?.optString("text") ?: "No analysis feedback received."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during analysis calling", e)
            "⚠️ **Network Error**: Unable to contact the Gemini feedback compiler. Please check your connection."
        }
    }

    private fun buildAnalysisPrompt(
        stats: UserStats,
        completedTasks: List<Task>,
        missedTasks: List<Task>,
        allTasks: List<Task>
    ): String {
        return """
            You are the "UntilDone Drill Master" AI. Your job is to analyze the user's procrastination patterns, diagnose scheduling weaknesses, reward their successes with intense praise, and discipline their failures with aggressive, motivating psychology. Do not use generic, slow corporate talk. Speak like a passionate extreme productivity system that demands discipline.

            USER METRICS:
            - Accountability Level: ${stats.level} 
            - Cumulative Experience (XP): ${stats.xp} (next level in ${stats.level * 150} XP)
            - Current Streak: ${stats.currentStreak} consecutive tasks
            - Max Record Streak: ${stats.maxStreak} tasks
            - Focus Integrity Score: ${stats.dailyScore}/100 
            - Fully Completed Tasks: ${stats.completedCount}
            - Failed/Missed Alarms: ${stats.missedCount}

            RECENT TASKS HISTORY:
            ${
                allTasks.take(15).joinToString("\n") { task ->
                    val status = if (task.completed) "COMPLETED" else if (task.missed) "MISSED" else "PENDING"
                    "- '${task.title}' [Category: ${task.category}, Priority: ${task.priority}, Status: $status, Snoozes: ${task.snoozedCount}]"
                }
            }

            Based on these metrics and tasks, prepare a highly structured, compact report in Markdown.
            Include:
            1. 📊 **SITUATION REPORT (SITREP)**: A snappy, high-impact review of their accountability status.
            2. 🛠️ **DIAGNOSIS**: Highlight their weak spots, potential procrastination category or hours (e.g. repeated snooze count, gym tasks missed vs completed).
            3. ⚡ **PREDICTIVE RISK**: Highlight what tasks they are at risk of neglecting next based on trends.
            4. 🦾 **DRILL INSTRUCTIONS**: 3 bulleted actionable tactical suggestions to destroy procrastination.
            5. 🔥 **MOTIVATIONAL SPITFIRE**: A short, direct, psychologically aggressive closing statement from the Drill Master.

            Keep it extremely bold and readable, with generous emoji accents, styled perfectly for a dark modern futuristic mobile view. Use a bold, commanding, and respectful tone.
        """.trimIndent()
    }
}
