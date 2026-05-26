package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
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
import com.example.ui.UntilDoneViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(viewModel: UntilDoneViewModel) {
    val aiResponse by viewModel.aiAnalysis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

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
            text = "🧠 DRILL MASTER AI ASSISTANT",
            style = MaterialTheme.typography.headlineSmall,
            color = NeonPink,
            fontSize = 20.sp
        )

        Text(
            text = "Your cognitive behavioral performance checker. The Drill Master inspects your snooze habits, completed checklists, and penalised streaks to deliver severe corrective feedback.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        // General Status Glow
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Assistant",
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "ACCOUNTABILITY ANALYSIS SYSTEM",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    "Compile a complete tactical SITREP of your recent habits. Detect lazy snooze loops, study bypasses, and unlock targeted guidelines.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                if (isAnalyzing) {
                    // Loading State with glowing scanner border animation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCard)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "DRILL MASTER DECRYPTING YOUR HABITS...",
                            color = NeonCyan.copy(alpha = pulseAlpha),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.runAiAnalysis() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = NeonCyan, shape = RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Sparkle", tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚡ COMPILE INTEGRITY SITREP", color = NeonCyan, fontSize = 13.sp)
                    }
                }
            }
        }

        // Analysis Output Panel
        Text("DRILL DEPLOYMENT DATA", color = Color.White, fontSize = 12.sp)

        if (aiResponse == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty Report",
                        tint = TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        "NO CURRENT SITREP COMPILED",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap 'COMPILE INTEGRITY SITREP' for real-time cognitive auditing. This utilizes your exact completion databases and levels.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📜 DRILL MASTER AUDIT REPORT",
                        color = NeonPink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider(color = BorderColor)

                    // Render lines nicely
                    val lines = aiResponse!!.split("\n")
                    lines.forEach { line ->
                        if (line.isNotEmpty()) {
                            Text(
                                text = line,
                                color = if (line.startsWith("#")) NeonCyan else Color.White,
                                fontSize = if (line.startsWith("#")) 15.sp else 13.sp,
                                fontWeight = if (line.startsWith("#") || line.contains("**")) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
