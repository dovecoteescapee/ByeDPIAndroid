package io.github.dovecoteescapee.byedpi.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.activities.MainActivity
import io.github.dovecoteescapee.byedpi.data.*
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppMonitorService : LifecycleService() {

    companion object {
        private val TAG: String = AppMonitorService::class.java.simpleName
        private const val FOREGROUND_SERVICE_ID: Int = 3
        private const val NOTIFICATION_CHANNEL_ID: String = "ByeDPI AppMonitor"
        private const val POLL_INTERVAL_MS = 2500L

        const val ACTION_START_MONITOR = "start_monitor"
        const val ACTION_STOP_MONITOR = "stop_monitor"
    }

    private var monitorJob: Job? = null
    private var targetPackage: String? = null
    private var wasTargetInForeground = false
    private var manualOverride = false
    private var autoInitiatedAction = false

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                STOPPED_BROADCAST -> {
                    if (!autoInitiatedAction && wasTargetInForeground) {
                        Log.i(TAG, "Manual disconnect detected, setting override")
                        manualOverride = true
                    }
                }
                STARTED_BROADCAST -> {
                    if (!autoInitiatedAction && wasTargetInForeground) {
                        Log.i(TAG, "Manual connect detected, clearing override")
                        manualOverride = false
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerNotificationChannel()

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                statusReceiver,
                IntentFilter().apply {
                    addAction(STARTED_BROADCAST)
                    addAction(STOPPED_BROADCAST)
                },
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                statusReceiver,
                IntentFilter().apply {
                    addAction(STARTED_BROADCAST)
                    addAction(STOPPED_BROADCAST)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusReceiver)
        monitorJob?.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_MONITOR -> {
                startForegroundNotification()
                startMonitoring()
                return START_STICKY
            }
            ACTION_STOP_MONITOR -> {
                stopMonitoring()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Restarted by system after being killed
                if (getPreferences().getBoolean("auto_connect_enabled", false)) {
                    startForegroundNotification()
                    startMonitoring()
                    return START_STICKY
                }
                stopSelf()
                return START_NOT_STICKY
            }
        }
    }

    private fun startMonitoring() {
        val prefs = getPreferences()
        targetPackage = prefs.getString("auto_connect_package", null)

        if (targetPackage == null) {
            Log.w(TAG, "No target package configured")
            stopSelf()
            return
        }

        monitorJob?.cancel()
        monitorJob = lifecycleScope.launch {
            while (isActive) {
                val foregroundPkg = getForegroundPackage()
                val isTargetForeground = foregroundPkg == targetPackage

                if (isTargetForeground && !wasTargetInForeground) {
                    onTargetAppOpened()
                } else if (!isTargetForeground && wasTargetInForeground) {
                    onTargetAppClosed()
                }

                wasTargetInForeground = isTargetForeground
                delay(POLL_INTERVAL_MS)
            }
        }

        Log.i(TAG, "Started monitoring for $targetPackage")
    }

    private fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        Log.i(TAG, "Stopped monitoring")
    }

    private fun onTargetAppOpened() {
        Log.i(TAG, "Target app opened: $targetPackage")
        manualOverride = false

        val (status, _) = appStatus
        if (status == AppStatus.Running) {
            Log.i(TAG, "Already connected")
            return
        }

        val mode = getPreferences().mode()
        if (mode == Mode.VPN && VpnService.prepare(this) != null) {
            Log.w(TAG, "VPN permission not granted, cannot auto-connect")
            return
        }

        autoInitiatedAction = true
        ServiceManager.start(this, mode)
        autoInitiatedAction = false
    }

    private fun onTargetAppClosed() {
        Log.i(TAG, "Target app closed: $targetPackage")

        if (manualOverride) {
            Log.i(TAG, "Manual override active, resetting")
            manualOverride = false
            return
        }

        val (status, _) = appStatus
        if (status == AppStatus.Halted) {
            Log.i(TAG, "Already disconnected")
            return
        }

        autoInitiatedAction = true
        ServiceManager.stop(this)
        autoInitiatedAction = false
    }

    private fun getForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 5000

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            beginTime,
            endTime
        )

        if (usageStats.isNullOrEmpty()) return null

        return usageStats
            .filter { it.lastTimeUsed > 0 }
            .maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    private fun registerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.auto_connect_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val appName = targetPackage?.let {
            try {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(it, 0)
                ).toString()
            } catch (_: Exception) { it }
        } ?: "..."

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setSilent(true)
            .setContentTitle(getString(R.string.auto_connect_notification_title))
            .setContentText(getString(R.string.auto_connect_notification_content, appName))
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FOREGROUND_SERVICE_ID, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }
}
