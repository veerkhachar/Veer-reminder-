package com.example.focus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FocusStateHolder {
    private val _isFocusLockActive = MutableStateFlow(false)
    val isFocusLockActive: StateFlow<Boolean> = _isFocusLockActive

    private val _blockedPackages = MutableStateFlow(
        setOf(
            "com.instagram.android",          // Instagram
            "com.google.android.youtube",    // YouTube / Shorts
            "com.tiktok.android",            // TikTok
            "com.twitter.android",           // Twitter/X
            "com.facebook.katana",           // Facebook
            "com.snapchat.android"           // Snapchat
        )
    )
    val blockedPackages: StateFlow<Set<String>> = _blockedPackages

    fun setFocusLockActive(active: Boolean) {
        _isFocusLockActive.value = active
    }

    fun addBlockedPackage(pkg: String) {
        val current = _blockedPackages.value.toMutableSet()
        current.add(pkg)
        _blockedPackages.value = current
    }

    fun removeBlockedPackage(pkg: String) {
        val current = _blockedPackages.value.toMutableSet()
        current.remove(pkg)
        _blockedPackages.value = current
    }
}
