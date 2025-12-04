package io.github.dovecoteescapee.byedpi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.SystemClock
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_REBOOT ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // for A15, todo: use wasForceStopped
            if (SystemClock.elapsedRealtime() > 5 * 60 * 1000) {
                return
            }

            val preferences = context.getPreferences()
            val autorunEnabled = preferences.getBoolean("autostart", false)

            if(autorunEnabled) {
                when (preferences.mode()) {
                    Mode.VPN -> {
                        if (VpnService.prepare(context) == null) {
                            ServiceManager.start(context, Mode.VPN)
                        }
                    }

                    Mode.Proxy -> ServiceManager.start(context, Mode.Proxy)
                }
            }
        }
    }
}