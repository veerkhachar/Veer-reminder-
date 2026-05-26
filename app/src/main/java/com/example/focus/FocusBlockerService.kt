package com.example.focus

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity

class FocusBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Only block when Focus Lock Mode is activated in state
            if (FocusStateHolder.isFocusLockActive.value) {
                if (FocusStateHolder.blockedPackages.value.contains(packageName)) {
                    Log.w("FocusBlockerService", "Intercepted blacklisted package: $packageName")
                    
                    // Perform safe physical BACK global action
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    
                    // Re-route user back to UntilDone to reinforce goal and prompt task completion
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("SHOW_FOCUS_BLOCK_OVERLAY", true)
                        putExtra("BLOCKED_PACKAGE_NAME", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w("FocusBlockerService", "Focus blocker accessibility service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("FocusBlockerService", "Focus blocker accessibility service successfully initialized")
    }
}
