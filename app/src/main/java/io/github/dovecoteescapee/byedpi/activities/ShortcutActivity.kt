package io.github.dovecoteescapee.byedpi.activities

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class ShortcutActivity : Activity() {

    companion object {
        private val TAG: String = ShortcutActivity::class.java.simpleName
        const val ACTION_CONNECT = "io.github.dovecoteescapee.byedpi.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "io.github.dovecoteescapee.byedpi.ACTION_DISCONNECT"
        const val ACTION_TOGGLE = "io.github.dovecoteescapee.byedpi.ACTION_TOGGLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> disconnect()
            ACTION_TOGGLE -> toggle()
            else -> Log.w(TAG, "Unknown action: ${intent?.action}")
        }

        finish()
    }

    private fun connect() {
        val (status, _) = appStatus
        if (status == AppStatus.Running) {
            Log.i(TAG, "Already connected")
            return
        }

        val mode = getPreferences().mode()

        if (mode == Mode.VPN && VpnService.prepare(this) != null) {
            Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
            return
        }

        ServiceManager.start(this, mode)
    }

    private fun disconnect() {
        val (status, _) = appStatus
        if (status == AppStatus.Halted) {
            Log.i(TAG, "Already disconnected")
            return
        }

        ServiceManager.stop(this)
    }

    private fun toggle() {
        val (status, _) = appStatus
        when (status) {
            AppStatus.Halted -> connect()
            AppStatus.Running -> disconnect()
        }
    }
}
