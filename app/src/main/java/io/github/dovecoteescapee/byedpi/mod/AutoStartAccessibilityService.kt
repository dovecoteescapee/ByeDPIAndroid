package io.github.dovecoteescapee.byedpi.mod

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import io.github.dovecoteescapee.byedpi.activities.MainActivity

class AutoStartAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("isAutoStart", true)
        }
        startActivity(launchIntent)

        return super.onStartCommand(intent, flags, startId)
    }
}