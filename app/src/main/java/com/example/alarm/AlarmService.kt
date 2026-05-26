package com.example.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import kotlinx.coroutines.*

class AlarmService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var elapsedSeconds = 0
    private var timerJob: Job? = null
    
    private var taskId = -1
    private var taskTitle = ""

    companion object {
        private const val CHANNEL_ID = "UNTIL_DONE_ALARM_CHANNEL"
        private const val NOTIFICATION_ID = 9999
        private const val TAG = "AlarmService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskId = intent?.getIntExtra("TASK_ID", -1) ?: -1
        taskTitle = intent?.getStringExtra("TASK_TITLE") ?: "Pending Task"

        if (taskId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "Alarm active for task ID: $taskId - $taskTitle")

        // 1. Notify the live ActiveAlarmManager
        ActiveAlarmManager.startAlarm(taskId, taskTitle)

        // 2. Start Foreground Service
        val notification = buildNotification(taskTitle, "Soft Reminding... TAP to solve/complete!")
        startForeground(NOTIFICATION_ID, notification)

        // 3. Play Looping sound
        playAudio(0.3f) // Start soft

        // 4. Start Vibration
        vibratePhone(longArrayOf(0, 400, 800, 400), true) // Modest vibration pattern

        // 5. Start Escalation Timer Context
        startEscalationTimer()

        return START_STICKY
    }

    private fun startEscalationTimer() {
        timerJob?.cancel()
        elapsedSeconds = 0
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                elapsedSeconds++
                ActiveAlarmManager.updateElapsed(elapsedSeconds)

                // Escalation intervals check
                when (elapsedSeconds) {
                    120 -> { // 2 minutes - increase volume first stage
                        updateAudioVolume(0.6f)
                        updateNotification("Urgent Alarm Escalating", "Time is tickling... Get it done!")
                    }
                    300 -> { // 5 minutes - louder Sound
                        updateAudioVolume(1.0f)
                        vibratePhone(longArrayOf(0, 800, 200, 800), true) // Louder tone and hard vibration
                        updateNotification("Aggressive Alert Mode!", "5 Minutes passed procrastination detected!")
                    }
                    600 -> { // 10 minutes - heavy vibration
                        vibratePhone(longArrayOf(0, 1000, 100, 1000), true)
                        updateNotification("CRITICAL ALARM!", "10 minutes ignoring! Resolve now or lose streaks!")
                    }
                    900 -> { // 15 minutes - red screen warning activated via ActiveAlarmManager update
                        updateNotification("EMERGENCY LEVEL!", "Red warnings active. Complete task immediately!")
                    }
                    1200 -> { // 20 minutes - penalty failure automatically triggered!
                        autoFailTaskDueToIgnorance()
                        break
                    }
                }
            }
        }
    }

    private fun autoFailTaskDueToIgnorance() {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = TaskRepository(db)
            repository.failTask(taskId, "Task alarm ignored for over 20 minutes")
            
            // Show missed notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val missedNotification = NotificationCompat.Builder(this@AlarmService, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Goal Missed & Punished")
                .setContentText("You ignored '$taskTitle'. XP lost and streaks broken.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(taskId + 10000, missedNotification)

            Log.w(TAG, "Task $taskId auto-penalty completed.")
            stopSelf()
        }
    }

    private fun playAudio(volume: Float) {
        mediaPlayer?.release()
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize standard media player", e)
        }
    }

    private fun updateAudioVolume(volume: Float) {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.e(TAG, "Failed updating volume states", e)
        }
    }

    private fun vibratePhone(pattern: LongArray, loop: Boolean) {
        vibrator?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, if (loop) 0 else -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, if (loop) 0 else -1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "UntilDone Accountability Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sends persistent alarms for action-lock verification"
                setSound(null, null) // Silent notification sound so mediaPlayer controls audio escalation
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(title: String, body: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(taskTitle, "$title: $body"))
    }

    override fun onDestroy() {
        Log.d(TAG, "Stopping active alarm service.")
        timerJob?.cancel()
        serviceScope.cancel()
        
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        
        ActiveAlarmManager.stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
