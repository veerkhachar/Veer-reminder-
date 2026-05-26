package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Base64
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationOverlay(
    task: Task,
    isRingingAlarm: Boolean,
    elapsedSeconds: Int,
    onComplete: (proofImage: String?, note: String?) -> Unit,
    onSnooze: () -> Unit,
    onClose: () -> Unit
) {
    // Alarm Strobe pulse background
    val infiniteTransition = rememberInfiniteTransition(label = "strobe")
    val strobeColor by infiniteTransition.animateColor(
        initialValue = if (isRingingAlarm) NeonRed.copy(alpha = 0.8f) else CyberBackground,
        targetValue = if (isRingingAlarm) Color.Black else CyberBackground,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_color"
    )

    // Verification Inputs
    var noteInput by remember { mutableStateOf("") }
    var mockProofImageBase64 by remember { mutableStateOf<String?>(null) }
    var isSnappingPhoto by remember { mutableStateOf(false) }

    // Focus Lock Timer inside app
    var timerSecondsRemaining by remember { mutableStateOf(60) } // 1 minute focus check for rapid test
    var isTimerActive by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerActive, timerSecondsRemaining) {
        if (isTimerActive && timerSecondsRemaining > 0) {
            delay(1000)
            timerSecondsRemaining--
            if (timerSecondsRemaining == 0) {
                // Done! Automatically complete task!
                onComplete(mockProofImageBase64, "Focus Timer task check successfully verified.")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(strobeColor)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurface)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            // Siren header
            if (isRingingAlarm) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert siren ringing",
                        tint = NeonRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UNTILDONE LOCK ENGAGED! (${elapsedSeconds}s)",
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "🔒 SUBMIT VERIFICATION PROOF",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = task.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Verification: ${task.proofType.replace("_", " ")}",
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Divider(color = BorderColor)

            // Dynamic Render based on proofType
            when (task.proofType) {
                "COMPLETED_TAP" -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "HONOR PROTOCOL VERIFICATION\nTap below to confirm you swear upon your level that this is fully complete. Procrastinating ruins streaks.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onComplete(null, "Verified under Tap-Honour protocol.") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text("I SWEAR I AM COMPLETED", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "SCREENSHOT" -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "SCREENSHOT CAPTURE SUBMISSION",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (mockProofImageBase64 == null) {
                            // Viewfinder representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSnappingPhoto) {
                                    CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = TextSecondary, modifier = Modifier.size(28.dp))
                                        Text("Camera Viewfinder active", color = TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    isSnappingPhoto = true
                                    // Generate a mock green verification image and convert to Base64 base
                                    val bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(bitmap)
                                    val paint = Paint().apply {
                                        color = android.graphics.Color.GREEN
                                        style = Paint.Style.FILL
                                    }
                                    canvas.drawRect(0f, 0f, 150f, 150f, paint)
                                    val out = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                                    val bytes = out.toByteArray()
                                    mockProofImageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                                    isSnappingPhoto = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                                modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = NeonCyan, shape = RoundedCornerShape(10.dp))
                            ) {
                                Text("📸 CAPTURE SCREENSHOT PROOF", color = NeonCyan)
                            }
                        } else {
                            // Display Snapped Proof preview
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCard)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("verification_screenshot.jpg", color = Color.White, fontSize = 12.sp)
                                    Text("Image capture locked successfully", color = NeonGreen, fontSize = 10.sp)
                                }
                                IconButton(onClick = { mockProofImageBase64 = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = NeonRed, modifier = Modifier.size(16.dp))
                                }
                            }

                            Button(
                                onClick = { onComplete(mockProofImageBase64, "Screenshot evidence submitted successfully.") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Text("SUBMIT PROOF FILE", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "TIMER" -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "LOCKDOWN CONCENTRATION TIMER",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Progress circle representation
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = timerSecondsRemaining.toFloat() / 60f,
                                modifier = Modifier.fillMaxSize(),
                                color = NeonCyan,
                                trackColor = BorderColor,
                                strokeWidth = 8.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${timerSecondsRemaining}s", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("remaining", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        if (!isTimerActive) {
                            Button(
                                onClick = { isTimerActive = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("START FOCUS WORK 60S", color = Color.White)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TacticalYellow.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = "Focusing", tint = TacticalYellow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Countdown active. Sirens paused.", color = TacticalYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "NOTE" -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "WRITTEN REFLECTION VERIFICATION",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            label = { Text("What did you complete? (Min 15 chars)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        val canSubmit = noteInput.length >= 15

                        Button(
                            onClick = { if (canSubmit) onComplete(null, noteInput) },
                            enabled = canSubmit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canSubmit) NeonGreen else CyberCard,
                                disabledContainerColor = CyberSurface
                            ),
                            modifier = Modifier.fillMaxWidth().height(46.dp).border(width = 1.dp, color = if (canSubmit) NeonGreen else BorderColor, shape = RoundedCornerShape(23.dp))
                        ) {
                            Text("CONFIRM VERIFICATION NOTE", color = if (canSubmit) Color.Black else TextSecondary)
                        }
                    }
                }
            }

            Divider(color = BorderColor.copy(alpha = 0.5f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Snooze Alarm delay - penalty applies to XP
                Button(
                    onClick = onSnooze,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                    modifier = Modifier.weight(1f).border(width = 1.dp, color = TacticalYellow, shape = RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = "Snooze", tint = TacticalYellow)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snooze (5m)", color = TacticalYellow, fontSize = 11.sp)
                }

                if (!isRingingAlarm) {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                        modifier = Modifier.weight(1f).border(width = 1.dp, color = TextSecondary, shape = RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("BACK", color = TextSecondary)
                    }
                }
            }
        }
    }
}
