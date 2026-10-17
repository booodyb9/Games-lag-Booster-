package com.example

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Explicitly user-enabled accessibility service. It currently does not inspect,
 * store, transmit, click, or otherwise act on screen content.
 */
class GlassBoostAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
}
