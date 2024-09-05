package io.github.dovecoteescapee.byedpi.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BOOT_COMPLETED != action
            && Intent.ACTION_REBOOT != action
            && "android.intent.action.QUICKBOOT_POWERON" != action
        ) {
            return
        }

        val prefs = context.getPreferences()

        if (!prefs.getBoolean("autostart", false) || !prefs.getBoolean("was_running", false)) {
            return
        }

        val mode = prefs.mode()
        if (prefs.mode() == Mode.VPN && VpnService.prepare(context) != null) {
            return
        }

        ServiceManager.start(context, mode)
    }
}