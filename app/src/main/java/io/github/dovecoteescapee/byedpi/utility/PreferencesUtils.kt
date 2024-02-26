package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.github.dovecoteescapee.byedpi.data.Mode

fun getPreferences(context: Context): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(context)

fun SharedPreferences.getStringNotNull(key: String, defValue: String): String =
    getString(key, defValue) ?: defValue

fun SharedPreferences.mode(): Mode =
    Mode.fromString(getStringNotNull("byedpi_mode", "vpn"))
