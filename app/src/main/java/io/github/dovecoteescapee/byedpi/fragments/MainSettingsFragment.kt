package io.github.dovecoteescapee.byedpi.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.utility.AccessibilityUtils
import io.github.dovecoteescapee.byedpi.services.AutoStartAccessibilityService
import io.github.dovecoteescapee.byedpi.utility.*

class MainSettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private val TAG: String = MainSettingsFragment::class.java.simpleName

        fun setTheme(name: String) =
            themeByName(name)?.let {
                AppCompatDelegate.setDefaultNightMode(it)
            } ?: throw IllegalStateException("Invalid value for app_theme: $name")

        private fun themeByName(name: String): Int? = when (name) {
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> {
                Log.w(TAG, "Invalid value for app_theme: $name")
                null
            }
        }
    }

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            updatePreferences()
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_settings, rootKey)

        setEditTextPreferenceListener("dns_ip") {
            it.isBlank() || checkNotLocalIp(it)
        }

        findPreferenceNotNull<ListPreference>("app_theme")
            .setOnPreferenceChangeListener { _, newValue ->
                setTheme(newValue as String)
                true
            }

        val switchCommandLineSettings = findPreferenceNotNull<SwitchPreference>(
            "byedpi_enable_cmd_settings"
        )
        val uiSettings = findPreferenceNotNull<Preference>("byedpi_ui_settings")
        val cmdSettings = findPreferenceNotNull<Preference>("byedpi_cmd_settings")

        val setByeDpiSettingsMode = { enable: Boolean ->
            uiSettings.isEnabled = !enable
            cmdSettings.isEnabled = enable
        }

        setByeDpiSettingsMode(switchCommandLineSettings.isChecked)

        switchCommandLineSettings.setOnPreferenceChangeListener { _, newValue ->
            setByeDpiSettingsMode(newValue as Boolean)
            true
        }

        findPreferenceNotNull<Preference>("version").summary = BuildConfig.VERSION_NAME

        // mod
        val accessibilityStatusPref = findPreference<Preference>("accessibility_service_status")
        accessibilityStatusPref?.setOnPreferenceClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            true
        }

        val selectedApps = findPreference<Preference>("selected_apps")
        selectedApps?.setOnPreferenceClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.settings, AppSelectionFragment())
                .addToBackStack(null)
                .commit()
            true
        }

        updateAccessibilityStatus(accessibilityStatusPref)
        updatePreferences()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceListener)

        // mod
        val accessibilityStatusPref = findPreference<Preference>("accessibility_service_status")
        updateAccessibilityStatus(accessibilityStatusPref)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    private fun updatePreferences() {
        val mode = findPreferenceNotNull<ListPreference>("byedpi_mode")
            .value.let { Mode.fromString(it) }
        val dns = findPreferenceNotNull<EditTextPreference>("dns_ip")
        val ipv6 = findPreferenceNotNull<SwitchPreference>("ipv6_enable")

        // mod
        val applist_type = findPreferenceNotNull<ListPreference>("applist_type")
        val selected_apps = findPreferenceNotNull<Preference>("selected_apps")

        when (mode) {
            Mode.VPN -> {
                dns.isVisible = true
                ipv6.isVisible = true
                // mod
                when (applist_type.value) {
                    "disable" -> {
                        applist_type.isVisible = true
                        selected_apps.isVisible = false
                    }
                    "blacklist", "whitelist" -> {
                        applist_type.isVisible = true
                        selected_apps.isVisible = true
                    }
                    else -> {
                        applist_type.isVisible = true
                        selected_apps.isVisible = false
                        Log.w(TAG, "Unexpected applist_type value: ${applist_type.value}")
                    }
                }
            }

            Mode.Proxy -> {
                dns.isVisible = false
                ipv6.isVisible = false
                // mod
                applist_type.isVisible = false
                selected_apps.isVisible = false
            }
        }
    }

    // mod
    private fun updateAccessibilityStatus(preference: Preference?) {
        preference?.let {
            val isEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(
                requireContext(),
                AutoStartAccessibilityService::class.java
            )
            it.summary = if (isEnabled) {
                getString(R.string.accessibility_service_enabled)
            } else {
                getString(R.string.accessibility_service_disabled)
            }
        }
    }
}
