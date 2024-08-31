package io.github.dovecoteescapee.byedpi.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.mod.AccessibilityUtils
import io.github.dovecoteescapee.byedpi.mod.AutoStartAccessibilityService
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
        updateAccessibilityStatus(accessibilityStatusPref)

        val (appNames, packageNames) = getInstalledApps(requireContext())

        val multiSelectListPreference = findPreference<MultiSelectListPreference>("selected_apps")
        multiSelectListPreference?.entries = appNames
        multiSelectListPreference?.entryValues = packageNames

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
        val selected_apps = findPreferenceNotNull<MultiSelectListPreference>("selected_apps")

        when (mode) {
            Mode.VPN -> {
                dns.isVisible = true
                ipv6.isVisible = true
                // mod
                applist_type.isVisible = true
                selected_apps.isVisible = true
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

    private fun getInstalledApps(context: Context): Pair<Array<String>, Array<String>> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val appNamePackages = mutableListOf<Pair<String, String>>()

        for (app in apps) {
            val appName = pm.getApplicationLabel(app).toString()
            val packageName = app.packageName

            val canBeDisabled = try {
                val isEnabled = pm.getApplicationEnabledSetting(packageName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                isEnabled
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            if (appName.isNotBlank() && appName != packageName && canBeDisabled) {
                appNamePackages.add(Pair(appName, packageName))
            }
        }

        appNamePackages.sortBy { it.first }

        val sortedAppNames = appNamePackages.map { it.first }.toTypedArray()
        val sortedPackageNames = appNamePackages.map { it.second }.toTypedArray()

        return Pair(sortedAppNames, sortedPackageNames)
    }
}
