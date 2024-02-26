package io.github.dovecoteescapee.byedpi.core

import java.io.IOException

class ByeDpiProxy {
    companion object {
        init {
            System.loadLibrary("byedpi")
        }
    }

    private val fd = jniEventFd()

    init {
        if (fd < 0) {
            throw IOException("Failed to create eventfd")
        }
    }

    fun startProxy(preferences: ByeDpiProxyPreferences): Int =
        jniStartProxy(
            eventFd = fd,
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
            hostMixedCase = preferences.hostMixedCase,
            domainMixedCase = preferences.domainMixedCase,
            hostRemoveSpaces = preferences.hostRemoveSpaces,
            tlsRecordSplit = preferences.tlsRecordSplit,
            tlsRecordSplitPosition = preferences.tlsRecordSplitPosition,
            tlsRecordSplitAtSni = preferences.tlsRecordSplitAtSni,
        )

    fun stopProxy(): Int = jniStopProxy(fd)

    private external fun jniEventFd(): Int

    private external fun jniStartProxy(
        eventFd: Int,
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
        hostMixedCase: Boolean,
        domainMixedCase: Boolean,
        hostRemoveSpaces: Boolean,
        tlsRecordSplit: Boolean,
        tlsRecordSplitPosition: Int,
        tlsRecordSplitAtSni: Boolean,
    ): Int

    private external fun jniStopProxy(eventFd: Int): Int
}