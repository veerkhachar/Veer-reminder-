package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Task::class, UserStats::class, ActivityLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "until_done_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default UserStats on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.userStatsDao()?.insertOrUpdateStats(
                                UserStats(
                                    id = 1,
                                    xp = 0,
                                    level = 1,
                                    currentStreak = 0,
                                    maxStreak = 0,
                                    dailyScore = 100
                                )
                            )
                            INSTANCE?.activityLogDao()?.insertLog(
                                ActivityLog(
                                    actionType = "LEVEL_UP",
                                    taskTitle = "Start UntilDone",
                                    details = "Welcome to UntilDone! Your journey to extreme accountability starts now.",
                                    xpChange = 0
                                )
                            )
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
