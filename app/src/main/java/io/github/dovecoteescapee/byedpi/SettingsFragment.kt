package io.github.dovecoteescapee.byedpi

import android.net.InetAddresses
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private val TAG: String = SettingsFragment::class.java.simpleName

        private fun checkIp(ip: String): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                InetAddresses.isNumericAddress(ip)
            } else {
                Patterns.IP_ADDRESS.matcher(ip).matches()
            }

        private fun checkPort(port: String): Boolean =
            port.toIntOrNull()?.let { it in 1..65535 } ?: false
    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        setEditTestPreferenceListener("dns_ip") { checkIp(it) }
        setEditTestPreferenceListener("byedpi_port") { checkPort(it) }
        setEditTestPreferenceListener("byedpi_max_connections") { value ->
            value.toIntOrNull()?.let { it > 0 } ?: false
        }
        setEditTestPreferenceListener("byedpi_buffer_size") { value ->
            value.toIntOrNull()?.let { it > 0 } ?: false
        }
        setEditTestPreferenceListener("byedpi_default_ttl") { value ->
            value.toIntOrNull()?.let { it >= 0 } ?: false
        }
        setEditTestPreferenceListener("byedpi_split_position") { value ->
            value.toIntOrNull() != null
        }
        setEditTestPreferenceListener("byedpi_fake_ttl") { value ->
            value.toIntOrNull()?.let { it >= 0 } ?: false
        }
        setEditTestPreferenceListener("byedpi_tlsrec_position") {
            it.toIntOrNull()?.let { it >= 0 } ?: false
        }
    }

    private fun setEditTestPreferenceListener(key: String, check: (String) -> Boolean) {
        findPreference<EditTextPreference>(key)
            ?.setOnPreferenceChangeListener { preference, newValue ->
                newValue as String
                val valid = check(newValue)
                if (!valid) {
                    Log.e(TAG, "Invalid ${preference.title}: $newValue")
                }
                valid
            }
    }
}