package io.github.dovecoteescapee.byedpi.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.dovecoteescapee.byedpi.R

class ByeDpiCommandLineSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.byedpi_cmd_settings, rootKey)
    }
}
