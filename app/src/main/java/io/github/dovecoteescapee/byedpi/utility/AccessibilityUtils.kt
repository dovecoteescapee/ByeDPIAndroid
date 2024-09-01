package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object AccessibilityUtils {
    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            0
        }

        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                val splitter = TextUtils.SimpleStringSplitter(':')
                splitter.setString(settingValue)
                while (splitter.hasNext()) {
                    val accessibilityService = splitter.next()
                    if (accessibilityService.equals("${context.packageName}/${serviceClass.canonicalName}", ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}