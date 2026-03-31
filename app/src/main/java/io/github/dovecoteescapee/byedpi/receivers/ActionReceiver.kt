package io.github.dovecoteescapee.byedpi.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class ActionReceiver : BroadcastReceiver() {

    companion object {
        private val TAG: String = ActionReceiver::class.java.simpleName
        const val ACTION_CONNECT = "io.github.dovecoteescapee.byedpi.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "io.github.dovecoteescapee.byedpi.ACTION_DISCONNECT"
        const val ACTION_TOGGLE = "io.github.dovecoteescapee.byedpi.ACTION_TOGGLE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action: ${intent.action}")

        when (intent.action) {
            ACTION_CONNECT -> connect(context)
            ACTION_DISCONNECT -> disconnect(context)
            ACTION_TOGGLE -> toggle(context)
            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }

    private fun connect(context: Context) {
        val (status, _) = appStatus
        if (status == AppStatus.Running) {
            Log.i(TAG, "Already connected")
            return
        }

        val mode = context.getPreferences().mode()

        if (mode == Mode.VPN && VpnService.prepare(context) != null) {
            Log.w(TAG, "VPN permission not granted, cannot start from background")
            return
        }

        ServiceManager.start(context, mode)
    }

    private fun disconnect(context: Context) {
        val (status, _) = appStatus
        if (status == AppStatus.Halted) {
            Log.i(TAG, "Already disconnected")
            return
        }

        ServiceManager.stop(context)
    }

    private fun toggle(context: Context) {
        val (status, _) = appStatus
        when (status) {
            AppStatus.Halted -> connect(context)
            AppStatus.Running -> disconnect(context)
        }
    }
}
