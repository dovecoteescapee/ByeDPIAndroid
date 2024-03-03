package io.github.dovecoteescapee.byedpi.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class ByeDpiProxy {
    companion object {
        init {
            System.loadLibrary("byedpi")
        }
    }

    private val mutex = Mutex()
    private var fd = -1

    suspend fun startProxy(preferences: ByeDpiProxyPreferences): Int =
        jniStartProxy(createSocket(preferences))

    suspend fun stopProxy(): Int {
        mutex.withLock {
            if (fd < 0) {
                throw IllegalStateException("Proxy is not running")
            }

            val result = jniStopProxy(fd)
            if (result == 0) {
                fd = -1
            }
            return result
        }
    }

    private suspend fun createSocket(preferences: ByeDpiProxyPreferences): Int =
        mutex.withLock {
            if (fd >= 0) {
                throw IllegalStateException("Proxy is already running")
            }

            val fd = jniCreateSocket(
                ip = preferences.ip,
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
                fakeSni = preferences.fakeSni,
                oobData = preferences.oobData,
                hostMixedCase = preferences.hostMixedCase,
                domainMixedCase = preferences.domainMixedCase,
                hostRemoveSpaces = preferences.hostRemoveSpaces,
                tlsRecordSplit = preferences.tlsRecordSplit,
                tlsRecordSplitPosition = preferences.tlsRecordSplitPosition,
                tlsRecordSplitAtSni = preferences.tlsRecordSplitAtSni,
            )

            if (fd < 0) {
                throw IOException("Failed to create socket")
            }

            this.fd = fd
            fd
        }

    private external fun jniCreateSocket(
        ip: String,
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
        fakeSni: String,
        oobData: String,
        hostMixedCase: Boolean,
        domainMixedCase: Boolean,
        hostRemoveSpaces: Boolean,
        tlsRecordSplit: Boolean,
        tlsRecordSplitPosition: Int,
        tlsRecordSplitAtSni: Boolean,
    ): Int

    private external fun jniStartProxy(fd: Int): Int

    private external fun jniStopProxy(fd: Int): Int
}