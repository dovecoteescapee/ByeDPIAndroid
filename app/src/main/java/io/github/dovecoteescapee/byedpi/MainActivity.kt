package io.github.dovecoteescapee.byedpi

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import io.github.dovecoteescapee.byedpi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.statusButton.setOnClickListener {
            when (ByeDpiVpnService.status) {
                Status.STOPPED -> {
                    val intentPrepare = VpnService.prepare(this)
                    if (intentPrepare != null) {
                        register.launch(intentPrepare)
                    } else {
                        startVpnService()
                    }
                    updateStatus(Status.RUNNING)
                }
                Status.RUNNING -> {
                    stopVpnService()
                    updateStatus(Status.STOPPED)
                }
            }
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            if (ByeDpiVpnService.status == Status.RUNNING) {
                Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT)
                    .show()
            } else {
                startActivity(intent)
            }
        }

        updateStatus(ByeDpiVpnService.status)
    }

    private fun startVpnService() {
        val intent = Intent(this, ByeDpiVpnService::class.java)
        intent.action = "start"
        startService(intent)
    }

    private fun stopVpnService() {
        val intent = Intent(this, ByeDpiVpnService::class.java)
        intent.action = "stop"
        startService(intent)
    }

    private fun updateStatus(status : Status) {
        when (status) {
            Status.STOPPED -> {
                binding.statusButton.setText(R.string.start)
            }
            Status.RUNNING -> {
                binding.statusButton.setText(R.string.stop)
            }
        }
    }

    companion object {
        // Used to load the 'byedpi' library on application startup.
        init {
            System.loadLibrary("byedpi")
        }
    }
}