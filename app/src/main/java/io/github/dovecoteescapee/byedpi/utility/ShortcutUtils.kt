package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.activities.ToggleActivity

object ShortcutUtils {

    fun update(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcuts = mutableListOf<ShortcutInfo>()

            val toggleIntent = Intent(context, ToggleActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val toggleShortcut = ShortcutInfo.Builder(context, "toggle_service")
                .setShortLabel(context.getString(R.string.toggle_connect))
                .setLongLabel(context.getString(R.string.toggle_connect))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_toggle))
                .setIntent(toggleIntent)
                .build()

            shortcuts.add(toggleShortcut)

            if (context.getPreferences().getBoolean("byedpi_enable_cmd_settings", false)) {
                val history = HistoryUtils(context)
                val pinned = history.getHistory().filter { it.pinned }.take(3)

                pinned.forEachIndexed { index, strategy ->
                    val strategyIntent = Intent(context, ToggleActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("strategy", strategy.text)
                    }

                    val fullLabel = strategy.name?.takeIf { it.isNotBlank() } ?: strategy.text
                    val shortLabel = if (fullLabel.length > 15) fullLabel.take(15) + "..." else fullLabel
                    val longLabel = if (fullLabel.length > 30) fullLabel.take(30) + "..." else fullLabel

                    val commandShortcut = ShortcutInfo.Builder(context, "strategy_$index")
                        .setShortLabel(shortLabel)
                        .setLongLabel(longLabel)
                        .setIcon(Icon.createWithResource(context, R.drawable.ic_pin))
                        .setIntent(strategyIntent)
                        .build()

                    shortcuts.add(commandShortcut)
                }
            }

            shortcutManager.dynamicShortcuts = shortcuts
        }
    }

}
