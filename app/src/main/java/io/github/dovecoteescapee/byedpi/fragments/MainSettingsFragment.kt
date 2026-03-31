package io.github.dovecoteescapee.byedpi.fragments

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
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

        findPreferenceNotNull<DropDownPreference>("app_theme")
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

        // Auto-connect setup
        setupAutoConnect()

        updatePreferences()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceListener)
        updateAutoConnectSummary()
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    private fun setupAutoConnect() {
        val autoConnectSwitch = findPreferenceNotNull<SwitchPreference>("auto_connect_enabled")
        val appPicker = findPreferenceNotNull<Preference>("auto_connect_app_picker")

        autoConnectSwitch.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled) {
                if (!hasUsageStatsPermission()) {
                    Toast.makeText(requireContext(), R.string.usage_access_required, Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    return@setOnPreferenceChangeListener false
                }
                val pkg = sharedPreferences?.getString("auto_connect_package", null)
                if (pkg != null) {
                    ServiceManager.startMonitor(requireContext())
                }
            } else {
                ServiceManager.stopMonitor(requireContext())
            }
            true
        }

        appPicker.setOnPreferenceClickListener {
            val dialog = AppPickerDialogFragment()
            dialog.show(parentFragmentManager, "app_picker")
            true
        }

        parentFragmentManager.setFragmentResultListener(
            AppPickerDialogFragment.RESULT_KEY,
            this
        ) { _, bundle ->
            val packageName = bundle.getString(AppPickerDialogFragment.RESULT_PACKAGE)
            val appName = bundle.getString(AppPickerDialogFragment.RESULT_APP_NAME)

            sharedPreferences?.edit()
                ?.putString("auto_connect_package", packageName)
                ?.putString("auto_connect_app_name", appName)
                ?.apply()

            updateAutoConnectSummary()

            if (sharedPreferences?.getBoolean("auto_connect_enabled", false) == true) {
                ServiceManager.startMonitor(requireContext())
            }
        }

        updateAutoConnectSummary()
    }

    private fun updateAutoConnectSummary() {
        val appPicker = findPreference<Preference>("auto_connect_app_picker") ?: return
        val appName = sharedPreferences?.getString("auto_connect_app_name", null)
        appPicker.summary = appName ?: getString(R.string.auto_connect_app_picker_summary)
    }

    @Suppress("DEPRECATION")
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            requireContext().packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun updatePreferences() {
        val mode = findPreferenceNotNull<ListPreference>("byedpi_mode")
            .value.let { Mode.fromString(it) }
        val dns = findPreferenceNotNull<EditTextPreference>("dns_ip")
        val ipv6 = findPreferenceNotNull<SwitchPreference>("ipv6_enable")

        when (mode) {
            Mode.VPN -> {
                dns.isVisible = true
                ipv6.isVisible = true
            }

            Mode.Proxy -> {
                dns.isVisible = false
                ipv6.isVisible = false
            }
        }
    }
}
