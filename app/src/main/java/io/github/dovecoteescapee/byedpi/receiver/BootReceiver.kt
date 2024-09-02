package io.github.dovecoteescapee.byedpi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.dovecoteescapee.byedpi.services.AutoStartAccessibilityService
import io.github.dovecoteescapee.byedpi.utility.AccessibilityUtils

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            val autorunEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(
                context,
                AutoStartAccessibilityService::class.java
            )

            if(autorunEnabled) {
                val serviceIntent = Intent(context, AutoStartAccessibilityService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
}