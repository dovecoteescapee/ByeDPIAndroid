package io.github.dovecoteescapee.byedpi.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.dovecoteescapee.byedpi.data.START_ACTION
import io.github.dovecoteescapee.byedpi.data.STOP_ACTION
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.FAILED_BROADCAST
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.data.SENDER
import io.github.dovecoteescapee.byedpi.data.STARTED_BROADCAST
import io.github.dovecoteescapee.byedpi.data.STOPPED_BROADCAST
import io.github.dovecoteescapee.byedpi.data.Sender
import io.github.dovecoteescapee.byedpi.fragments.SettingsFragment
import io.github.dovecoteescapee.byedpi.databinding.ActivityMainBinding
import io.github.dovecoteescapee.byedpi.services.ByeDpiProxyService
import io.github.dovecoteescapee.byedpi.services.ByeDpiVpnService
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName

        private var status: AppStatus = AppStatus.Halted
        private var mode: Mode = Mode.VPN

        private fun collectLogs(): String? =
            try {
                Runtime.getRuntime()
                    .exec("logcat *:I -d")
                    .inputStream.bufferedReader()
                    .use { it.readText() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to collect logs", e)
                null
            }
    }

    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startVpn()
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
                updateStatus(AppStatus.Halted)
            }
        }

    private val logsRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val logs = collectLogs()

                if (logs == null) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.logs_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val uri = it.data?.data ?: run {
                        Log.e(TAG, "No data in result")
                        return@launch
                    }
                    contentResolver.openOutputStream(uri)?.use {
                        try {
                            it.write(logs.toByteArray())
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to save logs", e)
                        }
                    } ?: run {
                        Log.e(TAG, "Failed to open output stream")
                    }
                }
            }
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received intent: ${intent?.action}")

            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return
            }

            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd)
            if (sender == null) {
                Log.w(TAG, "Received intent with unknown sender: $senderOrd")
                return
            }

            when (val action = intent.action) {
                STARTED_BROADCAST -> if (
                    status == AppStatus.Halted || status == AppStatus.Starting
                ) {
                    updateStatus(AppStatus.Running, Mode.fromSender(sender))
                } else {
                    Log.w(TAG, "Received STARTED while status is $status")
                }

                STOPPED_BROADCAST -> if (
                    mode == Mode.fromSender(sender) &&
                    (status == AppStatus.Running || status == AppStatus.Stopping)
                ) {
                    updateStatus(AppStatus.Halted)
                } else {
                    Log.w(TAG, "Received STOPPED $sender while status is $status")
                }

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_start, sender.name),
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateStatus(AppStatus.Halted)
                }

                else -> Log.w(TAG, "Unknown action: $action")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        binding.statusButton.setOnClickListener {
            when (status) {
                AppStatus.Halted -> start()
                AppStatus.Running -> stop()
                else -> {
                    // ignore
                }
            }
        }

        val theme = getPreferences(this)
            .getString("app_theme", null)
        SettingsFragment.setTheme(theme ?: "system")
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_settings -> {
                if (status == AppStatus.Halted) {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

            R.id.action_save_logs -> {
                val intent =
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, "byedpi.log")
                    }

                logsRegister.launch(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    private fun start() {
//        Starting and stopping is too fast
//        updateStatus(AppStatus.Starting)

        val preferences = getPreferences(this)
        when (val mode = preferences.getString("byedpi_mode", null) ?: "vpn") {
            "vpn" -> {
                val intentPrepare = VpnService.prepare(this)
                if (intentPrepare != null) {
                    vpnRegister.launch(intentPrepare)
                } else {
                    startVpn()
                }
            }

            "proxy" -> startProxy()
            else -> Log.e(TAG, "Unknown mode: $mode")
        }
    }

    private fun startVpn() {
        Log.i(TAG, "Starting VPN")
        val intent = Intent(this, ByeDpiVpnService::class.java)
        intent.action = START_ACTION
        startService(intent)
    }

    private fun startProxy() {
        Log.i(TAG, "Starting proxy")
        val intent = Intent(this, ByeDpiProxyService::class.java)
        intent.action = START_ACTION
        startService(intent)
    }

    private fun stop() {
//        Starting and stopping is too fast
//        updateStatus(AppStatus.Stopping)
        when (mode) {
            Mode.VPN -> stopVpn()
            Mode.Proxy -> stopProxy()
        }
    }

    private fun stopVpn() {
        Log.i(TAG, "Stopping VPN")
        val intent = Intent(this, ByeDpiVpnService::class.java)
        intent.action = STOP_ACTION
        startService(intent)
    }

    private fun stopProxy() {
        Log.i(TAG, "Stopping proxy")
        val intent = Intent(this, ByeDpiProxyService::class.java)
        intent.action = STOP_ACTION
        startService(intent)
    }

    private fun updateStatus(
        status: AppStatus = MainActivity.status,
        mode: Mode = MainActivity.mode,
    ) {
        Log.i(TAG, "Updating from ${MainActivity.status} to $status")

        MainActivity.mode = mode

        val preferences = getPreferences(this)
        val proxyIp = preferences.getString("byedpi_proxy_ip", null) ?: "127.0.0.1"
        val proxyPort = preferences.getString("byedpi_proxy_port", null) ?: "1080"
        binding.proxyAddress.text = getString(R.string.proxy_address, proxyIp, proxyPort)

        when (status) {
            AppStatus.Halted -> {
                val newMode = preferences.mode()
                MainActivity.mode = newMode
                when (newMode) {
                    Mode.VPN -> {
                        binding.statusText.setText(R.string.vpn_disconnected)
                        binding.statusButton.setText(R.string.vpn_connect)
                    }

                    Mode.Proxy -> {
                        binding.statusText.setText(R.string.proxy_down)
                        binding.statusButton.setText(R.string.proxy_start)
                    }
                }
                MainActivity.status = AppStatus.Halted
                binding.statusButton.isEnabled = true
            }

            AppStatus.Running -> {
                when (mode) {
                    Mode.VPN -> {
                        binding.statusText.setText(R.string.vpn_connected)
                        binding.statusButton.setText(R.string.vpn_disconnect)
                    }

                    Mode.Proxy -> {
                        binding.statusText.setText(R.string.proxy_up)
                        binding.statusButton.setText(R.string.proxy_stop)
                    }
                }
                MainActivity.status = AppStatus.Running
                binding.statusButton.isEnabled = true
            }

            AppStatus.Starting -> {
                if (MainActivity.status == AppStatus.Halted) {
                    when (mode) {
                        Mode.VPN -> {
                            binding.statusText.setText(R.string.vpn_connecting)
                        }

                        Mode.Proxy -> {
                            binding.statusText.setText(R.string.proxy_starting)
                        }
                    }
                    MainActivity.status = AppStatus.Starting
                    binding.statusButton.isEnabled = false
                }
            }

            AppStatus.Stopping -> {
                if (MainActivity.status == AppStatus.Running) {
                    when (mode) {
                        Mode.VPN -> {
                            binding.statusText.setText(R.string.vpn_disconnecting)
                        }

                        Mode.Proxy -> {
                            binding.statusText.setText(R.string.proxy_stopping)
                        }
                    }
                    MainActivity.status = AppStatus.Stopping
                    binding.statusButton.isEnabled = false
                }
            }
        }
    }
}