package io.github.dovecoteescapee.byedpi.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.VpnService
import android.view.accessibility.AccessibilityEvent
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class AutoStartAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (getPreferences().mode()) {
            Mode.VPN -> {
                if (VpnService.prepare(this) == null) {
                    ServiceManager.start(this, Mode.VPN)
                }
            }

            Mode.Proxy -> ServiceManager.start(this, Mode.Proxy)
        }

        return super.onStartCommand(intent, flags, startId)
    }
}