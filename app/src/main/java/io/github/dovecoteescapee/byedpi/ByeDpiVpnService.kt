package io.github.dovecoteescapee.byedpi

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.preference.PreferenceManager
import engine.Engine
import engine.Key

class ByeDpiVpnService : VpnService(), LifecycleOwner {
    private var proxyThread: Long = -1
    private var vpn: ParcelFileDescriptor? = null

    private val dispatcher = ServiceLifecycleDispatcher(this)
    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    companion object {
        private val TAG: String = ByeDpiVpnService::class.java.simpleName

        var status: Status = Status.STOPPED
            private set
    }

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            return when (intent.action) {
                "start" -> {
                    run()
                    START_STICKY
                }

                "stop" -> {
                    stop()
                    START_NOT_STICKY
                }

                else -> {
                    throw IllegalArgumentException("Unknown action ${intent.action}")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.i(TAG, "VPN revoked")
        stop()
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
        stop()
    }

    private fun run() {
        val preferences = getPreferences()
        status = Status.RUNNING

        if (proxyThread >= 0) {
            Log.w(TAG, "Proxy already running")
            return
        }

        proxyThread = startProxy(preferences)
        if (proxyThread < 0) {
            status = Status.STOPPED
            Log.e(TAG, "Proxy failed to start")
            return
        }

        val vpn = getBuilder().establish()
        this.vpn = vpn
        if (vpn == null) {
            Log.e(TAG, "VPN connection failed")
            return
        }

        Log.d(TAG, "fd: ${vpn.fd}")
        startTun2Socks(vpn.fd, preferences.port)
    }

    private fun getPreferences(): ByeDpiProxyPreferences {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        return ByeDpiProxyPreferences(
            port =
            sharedPreferences.getString("byedpi_proxy_port", null)?.toInt(),
            maxConnections =
            sharedPreferences.getString("byedpi_max_connections", null)?.toInt(),
            bufferSize =
            sharedPreferences.getString("byedpi_buffer_size", null)?.toInt(),
            defaultTtl =
            sharedPreferences.getString("byedpi_default_ttl", null)?.toInt(),
            noDomain =
            sharedPreferences.getBoolean("byedpi_no_domain", false),
            desyncKnown =
            sharedPreferences.getBoolean("byedpi_desync_known", false),
            desyncMethod =
            sharedPreferences.getString("byedpi_desync_method", null)
                ?.let { ByeDpiProxyPreferences.DesyncMethod.fromName(it) },
            splitPosition =
            sharedPreferences.getString("byedpi_split_position", null)?.toInt(),
            splitAtHost =
            sharedPreferences.getBoolean("byedpi_split_at_host", false),
            fakeTtl =
            sharedPreferences.getString("byedpi_fake_ttl", null)?.toInt(),
            hostMixedCase =
            sharedPreferences.getBoolean("byedpi_host_mixed_case", false),
            domainMixedCase =
            sharedPreferences.getBoolean("byedpi_domain_mixed_case", false),
            hostRemoveSpaces =
            sharedPreferences.getBoolean("byedpi_host_remove_spaces", false),
            tlsRecordSplit =
            sharedPreferences.getBoolean("byedpi_tlsrec", false),
            tlsRecordSplitPosition =
            sharedPreferences.getString("byedpi_tlsrec_position", null)?.toInt(),
            tlsRecordSplitAtSni =
            sharedPreferences.getBoolean("byedpi_tlsrec_at_sni", false),
        )
    }

    private fun stop() {
        status = Status.STOPPED
        stopTun2Socks()
        stopProxy()
    }

    private fun startProxy(preferences: ByeDpiProxyPreferences): Long {
        Log.i(TAG, "Proxy started")
        return jniStartProxy(
            port = preferences.port,
            maxConnections = preferences.maxConnections,
            bufferSize = preferences.bufferSize,
            defaultTtl = preferences.defaultTtl,
            noDomain = preferences.noDomain,
            desyncKnown = preferences.desyncKnown,
            desyncMethod = preferences.desyncMethod.ordinal,
            splitPosition = preferences.splitPosition,
            splitAtHost = preferences.splitAtHost,
            fakeTtl = preferences.fakeTtl,
            hostMixedCase = preferences.hostMixedCase,
            domainMixedCase = preferences.domainMixedCase,
            hostRemoveSpace = preferences.hostRemoveSpaces,
            tlsRecordSplit = preferences.tlsRecordSplit,
            tlsRecordSplitPosition = preferences.tlsRecordSplitPosition,
            tlsRecordSplitAtSni = preferences.tlsRecordSplitAtSni,
        )
    }

    private fun stopProxy() {
        if (proxyThread < 0) {
            Log.w(TAG, "Proxy not running")
        }
        jniStopProxy(proxyThread)
        proxyThread = -1
    }

    private fun startTun2Socks(fd: Int, port: Int) {
        val key = Key().apply {
            mark = 0
            mtu = 0
            device = "fd://$fd"

            setInterface("")
            logLevel = "debug"
            udpProxy = "direct://"
            tcpProxy = "socks5://127.0.0.1:$port"

            restAPI = ""
            tcpSendBufferSize = ""
            tcpReceiveBufferSize = ""
            tcpModerateReceiveBuffer = false
        }

        Engine.insert(key)

        Log.i(TAG, "Tun2Socks started")
        Engine.start()
    }

    private fun stopTun2Socks() {
        Log.i(TAG, "Tun2socks stopped")
        vpn?.close() ?: Log.w(TAG, "VPN not running")
    }

    private external fun jniStartProxy(
        port: Int,
        maxConnections: Int,
        bufferSize: Int,
        defaultTtl: Int,
        noDomain: Boolean,
        desyncKnown: Boolean,
        desyncMethod: Int,
        splitPosition: Int,
        splitAtHost: Boolean,
        fakeTtl: Int,
        hostMixedCase: Boolean,
        domainMixedCase: Boolean,
        hostRemoveSpace: Boolean,
        tlsRecordSplit: Boolean,
        tlsRecordSplitPosition: Int,
        tlsRecordSplitAtSni: Boolean,
    ): Long

    private external fun jniStopProxy(proxyThread: Long)

    private fun getBuilder(): Builder {
        val builder = Builder()
        builder.setSession("ByeDPI")
        builder.setConfigureIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
        )

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dns = sharedPreferences.getString("dns_ip", "9.9.9.9")!!

        builder.addAddress("10.10.10.10", 32)
        builder.addRoute("0.0.0.0", 0)
        builder.addRoute("0:0:0:0:0:0:0:0", 0)
        builder.addDnsServer(dns)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        builder.addDisallowedApplication("io.github.dovecoteescapee.byedpi")

        return builder
    }
}
