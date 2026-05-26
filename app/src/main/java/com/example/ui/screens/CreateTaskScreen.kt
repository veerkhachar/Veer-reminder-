package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UntilDoneViewModel
import com.example.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: UntilDoneViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Categories
    val categories = listOf(
        CategoryItem("Work", "💼", NeonCyan),
        CategoryItem("Gym", "🏋️", NeonPink),
        CategoryItem("Study", "📚", NeonGreen),
        CategoryItem("Habit", "🔄", TacticalYellow),
        CategoryItem("Upload", "📹", NeonCyan),
        CategoryItem("Custom", "⚙️", TextSecondary)
    )
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // Priorities
    val priorities = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
    var selectedPriority by remember { mutableStateOf("MEDIUM") }

    // Proof types
    val proofTypes = listOf(
        ProofItem("COMPLETED_TAP", "Simple Tap", "Confirm task completed, honor system.", Icons.Default.Check),
        ProofItem("SCREENSHOT", "Screenshot / Photo", "Requires uploading a JPEG verification capture.", Icons.Default.CameraAlt),
        ProofItem("TIMER", "Forced Timer", "Launches a lockdown countdown inside app to verify work.", Icons.Default.Timer),
        ProofItem("NOTE", "Summary Note", "Forces writing down a completion review before stop.", Icons.Default.EditNote)
    )
    var selectedProofType by remember { mutableStateOf(proofTypes[0]) }

    // Time calculations
    val calendar = remember { Calendar.getInstance() }
    var scheduledTimeMs by remember { mutableStateOf(System.currentTimeMillis() + 10 * 60 * 1000) } // Default 10 min
    var isImportant by remember { mutableStateOf(true) }

    // Helper to calculate relative offset times
    fun setRelativeTime(minutes: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutes)
        scheduledTimeMs = cal.timeInMillis
    }

    val dateFormater = remember { java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading
        Text(
            text = "⚡ TRIGGER FORCE PROTOCOL",
            style = MaterialTheme.typography.headlineSmall,
            color = NeonPink,
            fontSize = 20.sp
        )

        Text(
            text = "Enter task metadata and lock constraints. Once scheduled, an aggressive alarm sequence will trigger. Procrastination is punished.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Task Description / Rules", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )

        // Category Chips Row
        Text("CATEGORY", color = Color.White, fontSize = 12.sp, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState(), enabled = false),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) cat.color.copy(alpha = 0.2f) else CyberSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) cat.color else BorderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("${cat.icon} ${cat.name}", color = if (isSelected) cat.color else TextPrimary, fontSize = 13.sp)
                }
            }
        }

        // Priority Chips Row
        Text("PUNISHMENT STAKES (PRIORITY)", color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            priorities.forEach { priority ->
                val isSelected = priority == selectedPriority
                val badgeColor = when (priority) {
                    "LOW" -> NeonGreen
                    "MEDIUM" -> NeonCyan
                    "HIGH" -> TacticalYellow
                    "CRITICAL" -> NeonRed
                    else -> TextSecondary
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else CyberSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) badgeColor else BorderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPriority = priority }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priority,
                        color = if (isSelected) badgeColor else TextSecondary,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Verification constraints
        Text("VERIFICATION CONSTRAINTS", color = Color.White, fontSize = 12.sp)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            proofTypes.forEach { item ->
                val isSelected = item.id == selectedProofType.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyberCard else CyberSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonCyan else BorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedProofType = item }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) NeonCyan else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.title, color = if (isSelected) NeonCyan else Color.White, fontSize = 14.sp)
                        Text(item.description, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Focus Block Overlay check
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Focus Lock", tint = NeonCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Focus Lock Intercept", color = Color.White, fontSize = 14.sp)
                    Text("Blocks distractions while pending", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Switch(
                checked = isImportant,
                onCheckedChange = { isImportant = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.5f)
                )
            )
        }

        // Clock Setting
        Text("ALARM SCHEDULING", color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Test in 1m" to 1,
                "In 5 min" to 5,
                "In 30 min" to 30,
                "In 1 hr" to 60,
                "In 4 hr" to 240
            ).forEach { (label, minutes) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurface)
                        .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                        .clickable { setRelativeTime(minutes) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = NeonCyan, fontSize = 10.sp)
                }
            }
        }

        // Date Picker Trigger Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp))
                .clickable {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            cal.set(Calendar.YEAR, year)
                            cal.set(Calendar.MONTH, month)
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    cal.set(Calendar.HOUR_OF_DAY, hour)
                                    cal.set(Calendar.MINUTE, minute)
                                    cal.set(Calendar.SECOND, 0)
                                    scheduledTimeMs = cal.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = NeonPink)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Select Exact Date & Time", color = Color.White, fontSize = 14.sp)
            }
            Text(
                text = dateFormater.format(Date(scheduledTimeMs)),
                color = NeonCyan,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Force Arm Button
        Button(
            onClick = {
                if (title.isNotEmpty()) {
                    viewModel.addTask(
                        title = title,
                        description = description,
                        category = selectedCategory.name,
                        priority = selectedPriority,
                        exactTime = scheduledTimeMs,
                        deadline = scheduledTimeMs + 2 * 60 * 60 * 1000, // 2h after
                        repeatFrequency = "ONCE",
                        reminderInterval = 5,
                        proofType = selectedProofType.id,
                        isImportant = isImportant
                    )
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(NeonCyan, NeonPink)),
                    shape = RoundedCornerShape(26.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            Text("⚡ ENGAGE FORCE PROTOCOL", fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

data class CategoryItem(val name: String, val icon: String, val color: Color)
data class ProofItem(val id: String, val title: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
