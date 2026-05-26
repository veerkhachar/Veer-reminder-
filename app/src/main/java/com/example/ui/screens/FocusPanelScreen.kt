package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus.FocusStateHolder
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusPanelScreen() {
    val context = LocalContext.current
    val isFocusActive by FocusStateHolder.isFocusLockActive.collectAsState()
    val blockedApps by FocusStateHolder.blockedPackages.collectAsState()

    var customPackage by remember { mutableStateOf("") }

    val presetApps = listOf(
        PresetApp("Instagram", "com.instagram.android", "📷 Reels scroll-procrastination detector"),
        PresetApp("YouTube / Shorts", "com.google.android.youtube", "📹 Instant video loops distracter"),
        PresetApp("TikTok", "com.tiktok.android", "🎵 Dynamic vertical feeds infinite scroll"),
        PresetApp("X (Twitter)", "com.twitter.android", "🐦 Text news feed hyper distraction"),
        PresetApp("Facebook", "com.facebook.katana", "👥 Social scrolling profile tracker"),
        PresetApp("Snapchat", "com.snapchat.android", "👻 Continuous streak video sender")
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
            text = "🛡️ ACCESSIBILITY FOCUS SHIELD",
            style = MaterialTheme.typography.headlineSmall,
            color = NeonCyan,
            fontSize = 20.sp
        )

        Text(
            text = "Focus Lock blocks selected highly addictive applications by instantly intercepting and re-routing you back to your goals whenever they are accidentally launched with pending items active.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        // Accessibility Active Card
        Card(
            modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = "Security Status", tint = NeonGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Accessibility Service Setup", color = Color.White, fontSize = 15.sp)
                }

                Text(
                    text = "Android requires a micro accessibility privilege to monitor and block active processes. Enable 'UntilDone Focus Blocker' in settings to arm physical blocking.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handler
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💡 ENGAGE SYSTEM BLOCKER", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        // Global State Module
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(width = 1.dp, color = if (isFocusActive) NeonCyan else BorderColor, shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "FOCUS LOCK MODE: ${if (isFocusActive) "ACTIVE" else "DISARMED"}",
                    color = if (isFocusActive) NeonCyan else Color.White,
                    fontSize = 15.sp
                )
                Text("Enforce blockers during pending tasks", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = isFocusActive,
                onCheckedChange = { FocusStateHolder.setFocusLockActive(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.5f)
                )
            )
        }

        // Segment Blocker List
        Text("DISTRACTING BLACKLISTED APPLICATIONS", color = Color.White, fontSize = 12.sp)

        presetApps.forEach { app ->
            val isBlocked = blockedApps.contains(app.packageName)
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
                Column {
                    Text(app.name, color = Color.White, fontSize = 14.sp)
                    Text(app.packageName, color = TextSecondary, fontSize = 10.sp, style = MaterialTheme.typography.bodySmall)
                    Text(app.sub, color = TextSecondary, fontSize = 10.sp)
                }
                Checkbox(
                    checked = isBlocked,
                    onCheckedChange = { checked ->
                        if (checked) {
                            FocusStateHolder.addBlockedPackage(app.packageName)
                        } else {
                            FocusStateHolder.removeBlockedPackage(app.packageName)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeonCyan,
                        checkmarkColor = Color.Black
                    )
                )
            }
        }

        // Custom package blocker block
        Text("CUSTOM GAME / RESOURCE ID BLOCKER", color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customPackage,
                onValueChange = { customPackage = it },
                label = { Text("App Package Name (e.g. com.tencent.ig)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (customPackage.isNotEmpty()) {
                        FocusStateHolder.addBlockedPackage(customPackage)
                        customPackage = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                modifier = Modifier.height(56.dp).border(width = 1.dp, color = NeonCyan, shape = RoundedCornerShape(12.dp))
            ) {
                Text("ADD", color = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

data class PresetApp(val name: String, val packageName: String, val sub: String)
