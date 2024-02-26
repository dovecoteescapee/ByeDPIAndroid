package io.github.dovecoteescapee.byedpi.fragments

import android.net.InetAddresses
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private val TAG: String = SettingsFragment::class.java.simpleName

        fun setTheme(name: String): Boolean = when (val theme = themeByName(name)) {
            null -> {
                Log.w(TAG, "Invalid value for app_theme: $name")
                false
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(theme)
                true
            }
        }

        private fun themeByName(name: String): Int? = when (name) {
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> {
                Log.w(TAG, "Invalid value for app_theme: $name")
                null
            }
        }

        private fun checkIp(ip: String): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                InetAddresses.isNumericAddress(ip)
            } else {
                // This pattern doesn't not support IPv6
                Patterns.IP_ADDRESS.matcher(ip).matches()
            }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<DropDownPreference>("app_theme")
            ?.setOnPreferenceChangeListener { preference, newValue ->
                setTheme(newValue as String)
            }

        setEditTextPreferenceListener("dns_ip") { checkIp(it) }
        setEditTextPreferenceListener("byedpi_proxy_ip") { checkIp(it) }
        setEditTestPreferenceListenerPort("byedpi_proxy_port")
        setEditTestPreferenceListenerInt(
            "byedpi_max_connections",
            1,
            Short.MAX_VALUE.toInt()
        )
        setEditTestPreferenceListenerInt(
            "byedpi_buffer_size",
            1,
            Int.MAX_VALUE / 4
        )
        setEditTestPreferenceListenerInt("byedpi_default_ttl", 0, 255)
        setEditTestPreferenceListenerInt(
            "byedpi_split_position",
            Int.MIN_VALUE,
            Int.MAX_VALUE
        )
        setEditTestPreferenceListenerInt("byedpi_fake_ttl", 1, 255)
        setEditTestPreferenceListenerInt(
            "byedpi_tlsrec_position",
            2 * Short.MIN_VALUE,
            2 * Short.MAX_VALUE,
        )

        findPreference<Preference>("version")?.summary =
            BuildConfig.VERSION_NAME
    }

    private fun setEditTestPreferenceListenerPort(key: String) {
        setEditTestPreferenceListenerInt(key, 1, 65535)
    }

    private fun setEditTestPreferenceListenerInt(
        key: String,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE
    ) {
        setEditTextPreferenceListener(key) { value ->
            value.toIntOrNull()?.let { it in min..max } ?: false
        }
    }

    private fun setEditTextPreferenceListener(key: String, check: (String) -> Boolean) {
        findPreference<EditTextPreference>(key)
            ?.setOnPreferenceChangeListener { preference, newValue ->
                when (newValue) {
                    is String -> {
                        val valid = check(newValue)
                        if (!valid) {
                            Toast.makeText(
                                requireContext(),
                                "Invalid value for ${preference.title}: $newValue",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        valid
                    }

                    else -> {
                        Log.w(
                            TAG,
                            "Invalid type for ${preference.key}: " +
                                    "$newValue has type ${newValue::class.java}"
                        )
                        false
                    }
                }
            }
    }
}
