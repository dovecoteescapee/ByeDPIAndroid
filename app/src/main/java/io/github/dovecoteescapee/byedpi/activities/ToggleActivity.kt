package io.github.dovecoteescapee.byedpi.activities

import android.app.Activity
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.edit
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class ToggleActivity : Activity() {

    companion object {
        private const val TAG = "ToggleServiceActivity"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getPreferences()
        val strategy = intent.getStringExtra("strategy")
        val current = prefs.getString("byedpi_cmd_args", null)

        val onlyUpdate = intent.getBooleanExtra("only_update", false)
        val onlyStart = intent.getBooleanExtra("only_start", false)
        val onlyStop = intent.getBooleanExtra("only_stop", false)

        when {
            onlyUpdate -> {
                if (strategy != null && strategy != current) {
                    prefs.edit(commit = true) { putString("byedpi_cmd_args", strategy) }
                    Log.i(TAG, "Strategy updated to: $strategy")
                }
            }
            onlyStart -> {
                val (status) = appStatus
                if (status == AppStatus.Halted) {
                    startService()
                } else {
                    Log.i(TAG, "Service already running")
                }
            }
            onlyStop -> {
                val (status) = appStatus
                if (status == AppStatus.Running) {
                    stopService()
                } else {
                    Log.i(TAG, "Service already stopped")
                }
            }
            else -> {
                if (strategy != null && strategy != current) {
                    prefs.edit(commit = true) { putString("byedpi_cmd_args", strategy) }
                    Log.i(TAG, "Strategy updated to: $strategy")
                    toggleService(strategy)
                } else {
                    toggleService(null)
                }
            }
        }

        finish()
    }

    private fun startService() {
        val mode = prefs.mode()

        if (mode == Mode.VPN && VpnService.prepare(this) != null) {
            return
        }

        ServiceManager.start(this, mode)
        Log.i(TAG, "Toggle service start")
    }

    private fun stopService() {
        ServiceManager.stop(this)
        Log.i(TAG, "Toggle service stop")
    }

    private fun toggleService(strategy: String?) {
        val (status) = appStatus
        when (status) {
            AppStatus.Halted -> {
                startService()
            }
            AppStatus.Running -> {
                if (strategy != null) {
                    stopService()
                    waitForServiceStop { success ->
                        Log.i(TAG, "Service stop: $success")
                        startService()
                    }
                } else {
                    stopService()
                }
            }
        }
    }

    private fun waitForServiceStop(onComplete: (Boolean) -> Unit) {
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        fun check() {
            val (status) = appStatus
            val elapsed = System.currentTimeMillis() - startTime

            when {
                status == AppStatus.Halted -> {
                    Log.i(TAG, "Service stopped")
                    onComplete(true)
                }
                elapsed >= 3000L -> {
                    Log.w(TAG, "Timeout waiting for service to stop")
                    onComplete(false)
                }
                else -> handler.postDelayed({ check() }, 100L)
            }
        }

        check()
    }
}