package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarm.ActiveAlarmManager
import com.example.alarm.AlarmService
import com.example.data.Task
import com.example.ui.UntilDoneViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                MainAppContent(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Refresh intent parameters for distraction focus blocker redirects
        setIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(intent: Intent?) {
    val viewModel: UntilDoneViewModel = viewModel()
    val context = LocalContext.current

    // Navigation Stack Routing Controllers
    var currentScreen by remember { mutableStateOf("MAIN_BAR") } // MAIN_BAR, CREATE_TASK
    var selectedTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, FOCUS, AI, ANALYTICS

    // Solving manual task state
    var solvingTask by remember { mutableStateOf<Task?>(null) }

    // Intercept active ringing alarm from Service State Bridge
    val activeAlarmTaskId by ActiveAlarmManager.activeTaskId.collectAsState()
    val activeAlarmTaskTitle by ActiveAlarmManager.activeTaskTitle.collectAsState()
    val elapsedSeconds by ActiveAlarmManager.elapsedSeconds.collectAsState()

    // Detect social media distraction block interception redirects
    var showBlockOverlayAlert by remember { mutableStateOf(false) }
    var blockedAppPackageName by remember { mutableStateOf("") }

    LaunchedEffect(intent) {
        if (intent?.getBooleanExtra("SHOW_FOCUS_BLOCK_OVERLAY", false) == true) {
            blockedAppPackageName = intent.getStringExtra("BLOCKED_PACKAGE_NAME") ?: "Addictive App"
            showBlockOverlayAlert = true
        }
    }

    // Capture task structure for ringing alarm to pass to Verification overlay
    var ringingTask by remember { mutableStateOf<Task?>(null) }
    val allTasks by viewModel.tasks.collectAsState()

    LaunchedEffect(activeAlarmTaskId, allTasks) {
        if (activeAlarmTaskId != null) {
            ringingTask = allTasks.find { it.id == activeAlarmTaskId }
        } else {
            ringingTask = null
        }
    }

    val currentTabLabelColor = NeonCyan
    val currentTabIndicatorColor = NeonCyan.copy(alpha = 0.2f)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground),
        bottomBar = {
            if (currentScreen == "MAIN_BAR" && activeAlarmTaskId == null && solvingTask == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    NavigationBar(
                        containerColor = CyberSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(percent = 50))
                            .clip(RoundedCornerShape(percent = 50)),
                        windowInsets = WindowInsets(0.dp)
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == "DASHBOARD",
                            onClick = { selectedTab = "DASHBOARD" },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = currentTabLabelColor,
                                indicatorColor = currentTabIndicatorColor,
                                unselectedTextColor = TextSecondary,
                                unselectedIconColor = TextSecondary
                            ),
                            icon = { Icon(Icons.Default.TaskAlt, contentDescription = "Tasks") },
                            label = { Text("Tasks", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = selectedTab == "FOCUS",
                            onClick = { selectedTab = "FOCUS" },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = currentTabLabelColor,
                                indicatorColor = currentTabIndicatorColor,
                                unselectedTextColor = TextSecondary,
                                unselectedIconColor = TextSecondary
                            ),
                            icon = { Icon(Icons.Default.Lock, contentDescription = "Focus Shield") },
                            label = { Text("Focus Shield", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = selectedTab == "AI_ASSISTANT",
                            onClick = { selectedTab = "AI_ASSISTANT" },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = currentTabLabelColor,
                                indicatorColor = currentTabIndicatorColor,
                                unselectedTextColor = TextSecondary,
                                unselectedIconColor = TextSecondary
                            ),
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Drill AI") },
                            label = { Text("Drill AI", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = selectedTab == "ANALYTICS",
                            onClick = { selectedTab = "ANALYTICS" },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = currentTabLabelColor,
                                indicatorColor = currentTabIndicatorColor,
                                unselectedTextColor = TextSecondary,
                                unselectedIconColor = TextSecondary
                            ),
                            icon = { Icon(Icons.Default.QueryStats, contentDescription = "Metrics") },
                            label = { Text("Metrics", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Routing Engine
            if (activeAlarmTaskId != null && ringingTask != null) {
                // Alarm Ringing OVERLAY Lockdown View!
                VerificationOverlay(
                    task = ringingTask!!,
                    isRingingAlarm = true,
                    elapsedSeconds = elapsedSeconds,
                    onComplete = { img, note ->
                        // 1. Complete database task updates
                        viewModel.completeTask(ringingTask!!.id, img, note)
                        // 2. Terminate background ring service
                        context.stopService(Intent(context, AlarmService::class.java))
                        Toast.makeText(context, "Protocol Solved! XP rewarded.", Toast.LENGTH_LONG).show()
                    },
                    onSnooze = {
                        // 1. Snooze database metrics
                        viewModel.snoozeTask(ringingTask!!.id)
                        // 2. Turn off alarm temporarily (updates automatically on database triggers)
                        context.stopService(Intent(context, AlarmService::class.java))
                        Toast.makeText(context, "Task Postponed! Laziness penalty applied to stats.", Toast.LENGTH_SHORT).show()
                    },
                    onClose = {}
                )
            } else if (solvingTask != null) {
                // Manual Solve Dialogue
                VerificationOverlay(
                    task = solvingTask!!,
                    isRingingAlarm = false,
                    elapsedSeconds = 0,
                    onComplete = { img, note ->
                        viewModel.completeTask(solvingTask!!.id, img, note)
                        solvingTask = null
                        Toast.makeText(context, "Task Solved! XP gained.", Toast.LENGTH_SHORT).show()
                    },
                    onSnooze = {
                        viewModel.snoozeTask(solvingTask!!.id)
                        solvingTask = null
                        Toast.makeText(context, "Task Snoozed and delayed.", Toast.LENGTH_SHORT).show()
                    },
                    onClose = { solvingTask = null }
                )
            } else {
                // Standard Application State Layout
                when (currentScreen) {
                    "CREATE_TASK" -> {
                        CreateTaskScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = "MAIN_BAR" }
                        )
                    }
                    "MAIN_BAR" -> {
                        when (selectedTab) {
                            "DASHBOARD" -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToCreate = { currentScreen = "CREATE_TASK" },
                                onSolveTask = { solvingTask = it }
                            )
                            "FOCUS" -> FocusPanelScreen()
                            "AI_ASSISTANT" -> AIAssistantScreen(viewModel = viewModel)
                            "ANALYTICS" -> AnalyticsScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // Distraction Lock Dialog Intercepts
            if (showBlockOverlayAlert) {
                AlertDialog(
                    onDismissRequest = { showBlockOverlayAlert = false },
                    confirmButton = {
                        Button(
                            onClick = { showBlockOverlayAlert = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("UNDERSTOOD DRILL MASTER", color = Color.Black)
                        }
                    },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Distraction intercepted", tint = NeonRed, modifier = Modifier.size(36.dp)) },
                    title = { Text("⚡ DISTRACTION SHIELD INTERCEPTED", color = NeonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "Access to blocked packages (like '${blockedAppPackageName.substringAfterLast('.')}') was immediately terminated. You have active pending tasks running. Go solve your proof verify loops to unlock focus restriction!",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    },
                    containerColor = CyberSurface,
                    modifier = Modifier.border(width = 1.dp, color = NeonRed, shape = RoundedCornerShape(28.dp))
                )
            }
        }
    }
}
