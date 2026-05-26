package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.UntilDoneViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: UntilDoneViewModel,
    onNavigateToCreate: () -> Unit,
    onSolveTask: (Task) -> Unit
) {
    val stats by viewModel.userStats.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val missedTasks by viewModel.missedTasks.collectAsState()

    var selectedFilter by remember { mutableStateOf("PENDING") } // PENDING, COMPLETED, MISSED

    val currentTasks = when (selectedFilter) {
        "PENDING" -> tasks.filter { !it.completed && !it.missed }
        "COMPLETED" -> completedTasks
        "MISSED" -> missedTasks
        else -> tasks
    }

    // Dynamic Level formulas
    val currentLevel = stats?.level ?: 1
    val currentXp = stats?.xp ?: 0
    val xpRequired = currentLevel * 150
    val progressFraction = if (xpRequired > 0) currentXp.toFloat() / xpRequired.toFloat() else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER: USER STATS & LEVEL ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Level Avatar with cyber gradients inside a glowing glass boundary
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(NeonCyan, Color(0xFF2563EB))
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LV$currentLevel",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Title uppercase metadata and subtitle username
                Column {
                    Text(
                        text = "UNTILDONE",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "Alex Procrastinator",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Streak Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🔥", fontSize = 12.sp)
                Text(
                    text = "${stats?.currentStreak ?: 0} DAY STREAK",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
        }

        // --- SECTION: ESCALATING PENALTY FOCUS CARD ---
        val soonestPendingTask = tasks.filter { !it.completed && !it.missed }.minByOrNull { it.exactTime }
        if (soonestPendingTask != null) {
            // A pending committing task is active!
            val totalWindowMs = soonestPendingTask.deadline - soonestPendingTask.exactTime
            val elapsedMs = System.currentTimeMillis() - soonestPendingTask.exactTime
            val progressRatio = if (totalWindowMs > 0) (elapsedMs.toFloat() / totalWindowMs.toFloat()).coerceIn(0f, 1f) else 0.5f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonPink.copy(alpha = 0.2f))
                                .border(width = 1.dp, color = NeonPink.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (soonestPendingTask.priority == "CRITICAL") "ESCALATION STAGE 3" else "LOCKDOWN TIMER ACTIVE",
                                color = NeonPink,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        val isOverdue = System.currentTimeMillis() > soonestPendingTask.deadline
                        Text(
                            text = if (isOverdue) "ALARM OVERDUE" else "COMMIT TARGET",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = soonestPendingTask.title,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        
                        val formattedTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(soonestPendingTask.exactTime))
                        Text(
                            text = "Scheduled: $formattedTime / Proof: ${soonestPendingTask.proofType}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    // Progress Visual Accent Bar (glowing rose/pink)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressRatio)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(NeonPink, NeonPink.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    }

                    Text(
                        text = "Stop the penalty triggers: Upload verification proof below before scheduled countdown completes to avoid leveling points deduction.",
                        color = NeonPink.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    // Unified Action buttons inside the focal view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Solve action
                        Button(
                            onClick = { onSolveTask(soonestPendingTask) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📸", fontSize = 14.sp)
                                Text("RESOLVE PROOF", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Snooze action
                        Button(
                            onClick = { viewModel.snoozeTask(soonestPendingTask.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 13.sp, color = TacticalYellow)
                                Text("SNOOZE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Fallback for No Active committing Task
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🛡️ ALL SYSTEMS AT OPTIMAL SPEC", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("No Pending Commitments", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Your procrastination shields are highly charged. Tap below to fast-launch study or setup custom commitment locks.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // --- SECTION: GAMIFICATION PREVIEW ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CURRENT LEVEL RANK",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        val rankLabel = when {
                            currentLevel <= 3 -> "Accountable Recruit"
                            currentLevel <= 7 -> "Active Grind Champion"
                            currentLevel <= 11 -> "Elite Executor"
                            else -> "Giga Accountability Master"
                        }
                        Text(
                            text = rankLabel,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$currentXp / $xpRequired XP",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Split indicators representing fraction bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val stepsCount = 4
                    for (i in 0 until stepsCount) {
                        val limit = (i + 1).toFloat() / stepsCount.toFloat()
                        val previousLimit = i.toFloat() / stepsCount.toFloat()
                        val isFilled = progressFraction >= limit
                        val isPartiallyFilled = progressFraction > previousLimit && progressFraction < limit
                        
                        val fillPercentage = when {
                            isFilled -> 1.0f
                            isPartiallyFilled -> (progressFraction - previousLimit) / (limit - previousLimit)
                            else -> 0.0f
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(
                                    when (i) {
                                        0 -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                        stepsCount - 1 -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                        else -> RoundedCornerShape(0.dp)
                                    }
                                )
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            if (fillPercentage > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fillPercentage)
                                        .background(NeonCyan)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION: AI INSIGHT BUBBLE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x1F6366F1)) // Indigo styled tint
                .border(width = 1.dp, color = Color(0x336366F1), shape = RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🤖", fontSize = 20.sp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "AI INSIGHT ENGINE",
                        color = Color(0xFFA5B4FC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = if (stats?.currentStreak ?: 0 > 7) {
                            "Amazing consistency! You are maintaining your high success rate. Keep solving the rapid proof loops to maintain momentum."
                        } else {
                            "AI Analysis: You consistently miss tasks scheduled during peak social hours. Re-scheduling to mornings is recommended for 92% success rate."
                        },
                        color = Color(0xFFC7D2FE),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Quick Instants Templates row
        Text("QUICK ACTION TEMPLATES (TESTS IN 1-MIN)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                QuickLauncher("Study", "📚", NeonGreen, "TIMER"),
                QuickLauncher("Gym Work", "🏋️", NeonPink, "SCREENSHOT"),
                QuickLauncher("Upload Shorts", "📹", NeonCyan, "SCREENSHOT"),
                QuickLauncher("Check Work", "💼", TacticalYellow, "NOTE")
            ).forEach { item ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val triggerTime = System.currentTimeMillis() + 60 * 1000 // Fired in 1 minute!
                            viewModel.addTask(
                                title = "Do ${item.label}",
                                description = "Triggered from UntilDone rapid schedule presets.",
                                category = item.label,
                                priority = "HIGH",
                                exactTime = triggerTime,
                                deadline = triggerTime + 10 * 60 * 1000,
                                repeatFrequency = "ONCE",
                                reminderInterval = 5,
                                proofType = item.proofType,
                                isImportant = true
                            )
                        }
                        .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(item.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(item.proofType, color = TextSecondary, fontSize = 8.sp)
                    }
                }
            }
        }

        // Navigation filters & Create button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Horizontal Chips filter
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PENDING", "COMPLETED", "MISSED").forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberCard else CyberSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NeonCyan else BorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) NeonCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Create FAB shortcut button
            IconButton(
                onClick = onNavigateToCreate,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .border(width = 1.dp, color = NeonCyan, shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
        }

        // Task Feed Cards
        if (currentTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp))
                    .background(CyberSurface)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Empty list", tint = TextSecondary, modifier = Modifier.size(28.dp))
                    Text(
                        text = "No $selectedFilter Tasks scheduled.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentTasks.forEach { task ->
                    val priorityColor = when (task.priority) {
                        "LOW" -> NeonGreen
                        "MEDIUM" -> NeonCyan
                        "HIGH" -> TacticalYellow
                        "CRITICAL" -> NeonRed
                        else -> TextSecondary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category emoji/badge
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val catEmoji = when (task.category) {
                                        "Work" -> "💼"
                                        "Gym" -> "🏋️"
                                        "Study" -> "📚"
                                        "Habit" -> "🔄"
                                        "Upload" -> "📹"
                                        else -> "⚙️"
                                    }
                                    Text("$catEmoji ${task.category.uppercase()}  ", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(priorityColor.copy(alpha = 0.15f))
                                            .border(width = 1.dp, color = priorityColor, shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(task.priority, color = priorityColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Delete Button
                                IconButton(
                                    onClick = { viewModel.deleteTask(task) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Title & Description
                            Text(
                                text = task.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (task.description.isNotEmpty()) {
                                Text(
                                    text = task.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Divider(color = BorderColor.copy(alpha = 0.5f))

                            // Bottom meta details and Solve action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val formattedTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(task.exactTime))
                                    Text("Reminder: $formattedTime", color = Color.White, fontSize = 11.sp)
                                    Text("Proof: ${task.proofType.replace("_", " ")}", color = TextSecondary, fontSize = 9.sp)
                                }

                                if (!task.completed && !task.missed) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Snooze Task Button
                                        Button(
                                            onClick = { viewModel.snoozeTask(task.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.border(width = 1.dp, color = TacticalYellow, shape = RoundedCornerShape(8.dp)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Snooze, contentDescription = "Snooze", tint = TacticalYellow, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Snooze (-5XP)", color = TacticalYellow, fontSize = 10.sp)
                                        }

                                        // Complete Solve Button
                                        Button(
                                            onClick = { onSolveTask(task) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                        ) {
                                            Text("SOLVE Proof", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else if (task.completed) {
                                    Text("✅ VERIFIED STATUS", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("❌ PENALIZED MISS", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

data class QuickLauncher(val label: String, val icon: String, val color: Color, val proofType: String)
