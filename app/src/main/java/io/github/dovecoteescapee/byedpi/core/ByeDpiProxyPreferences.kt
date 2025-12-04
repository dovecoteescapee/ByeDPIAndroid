package io.github.dovecoteescapee.byedpi.core

import android.content.SharedPreferences
import android.util.Log
import io.github.dovecoteescapee.byedpi.utility.checkIpAndPortInCmd
import io.github.dovecoteescapee.byedpi.utility.getStringNotNull
import io.github.dovecoteescapee.byedpi.utility.shellSplit

sealed interface ByeDpiProxyPreferences {
    companion object {
        fun fromSharedPreferences(preferences: SharedPreferences): ByeDpiProxyPreferences =
            when (preferences.getBoolean("byedpi_enable_cmd_settings", false)) {
                true -> ByeDpiProxyCmdPreferences(preferences)
                false -> ByeDpiProxyUIPreferences(preferences)
            }
    }
}

class ByeDpiProxyCmdPreferences(val args: Array<String>) : ByeDpiProxyPreferences {
    constructor(preferences: SharedPreferences) : this(
        cmdToArgs(
            preferences.getStringNotNull("byedpi_cmd_args", "-Ku -a1 -An -o1 -At,r,s -d1"),
            preferences
        )
    )

    companion object {
        private fun cmdToArgs(cmd: String, preferences: SharedPreferences): Array<String> {
            val firstArgIndex = cmd.indexOf("-")
            val args = (if (firstArgIndex > 0) cmd.substring(firstArgIndex) else cmd).trim()

            Log.d("ProxyPref", "CMD: $args")

            val (cmdIp, cmdPort) = preferences.checkIpAndPortInCmd()
            val hasIp = cmdIp != null
            val hasPort = cmdPort != null

            val enableHttp = preferences.getBoolean("byedpi_http_connect", false)
            val hasHttp = args.contains("-G") || args.contains("--http-connect")

            val ip = preferences.getStringNotNull("byedpi_proxy_ip", "127.0.0.1")
            val port = preferences.getStringNotNull("byedpi_proxy_port", "1080")

            val prefix = buildString {
                if (!hasIp) append("--ip $ip ")
                if (!hasPort) append("--port $port ")
                if (enableHttp && !hasHttp) append("--http-connect ")
            }

            Log.d("ProxyPref", "Added from settings: $prefix")

            if (prefix.isNotEmpty()) {
                return arrayOf("ciadpi") + shellSplit("$prefix$args")
            }

            return arrayOf("ciadpi") + shellSplit(args)
        }
    }
}

class ByeDpiProxyUIPreferences(
    ip: String? = null,
    port: Int? = null,
    httpConnect: Boolean? = null,
    maxConnections: Int? = null,
    bufferSize: Int? = null,
    defaultTtl: Int? = null,
    noDomain: Boolean? = null,
    desyncHttp: Boolean? = null,
    desyncHttps: Boolean? = null,
    desyncUdp: Boolean? = null,
    desyncMethod: DesyncMethod? = null,
    splitPosition: Int? = null,
    splitAtHost: Boolean? = null,
    fakeTtl: Int? = null,
    fakeSni: String? = null,
    oobChar: String? = null,
    hostMixedCase: Boolean? = null,
    domainMixedCase: Boolean? = null,
    hostRemoveSpaces: Boolean? = null,
    tlsRecordSplit: Boolean? = null,
    tlsRecordSplitPosition: Int? = null,
    tlsRecordSplitAtSni: Boolean? = null,
    hostsMode: HostsMode? = null,
    hosts: String? = null,
    tcpFastOpen: Boolean? = null,
    udpFakeCount: Int? = null,
    dropSack: Boolean? = null,
    byedpiFakeOffset: Int? = null,
) : ByeDpiProxyPreferences {
    val ip: String = ip ?: "127.0.0.1"
    val port: Int = port ?: 1080
    val httpConnect: Boolean = httpConnect ?: false
    val maxConnections: Int = maxConnections ?: 512
    val bufferSize: Int = bufferSize ?: 16384
    val defaultTtl: Int = defaultTtl ?: 0
    val noDomain: Boolean = noDomain ?: false
    val desyncHttp: Boolean = desyncHttp ?: true
    val desyncHttps: Boolean = desyncHttps ?: true
    val desyncUdp: Boolean = desyncUdp ?: true
    val desyncMethod: DesyncMethod = desyncMethod ?: DesyncMethod.OOB
    val splitPosition: Int = splitPosition ?: 1
    val splitAtHost: Boolean = splitAtHost ?: false
    val fakeTtl: Int = fakeTtl ?: 8
    val fakeSni: String = fakeSni ?: "www.iana.org"
    val oobChar: Byte = (oobChar ?: "a")[0].code.toByte()
    val hostMixedCase: Boolean = hostMixedCase ?: false
    val domainMixedCase: Boolean = domainMixedCase ?: false
    val hostRemoveSpaces: Boolean = hostRemoveSpaces ?: false
    val tlsRecordSplit: Boolean = tlsRecordSplit ?: false
    val tlsRecordSplitPosition: Int = tlsRecordSplitPosition ?: 0
    val tlsRecordSplitAtSni: Boolean = tlsRecordSplitAtSni ?: false
    val tcpFastOpen: Boolean = tcpFastOpen ?: false
    val udpFakeCount: Int = udpFakeCount ?: 1
    val dropSack: Boolean = dropSack ?: false
    val fakeOffset: Int = byedpiFakeOffset ?: 0
    val hostsMode: HostsMode = if (hosts?.isBlank() != false) HostsMode.Disable else hostsMode ?: HostsMode.Disable
    val hosts: String? = if (this.hostsMode == HostsMode.Disable) null  else hosts?.trim()

    constructor(preferences: SharedPreferences) : this(
        ip = preferences.getString("byedpi_proxy_ip", null),
        port = preferences.getString("byedpi_proxy_port", null)?.toIntOrNull(),
        httpConnect = preferences.getBoolean("byedpi_http_connect", false),
        maxConnections = preferences.getString("byedpi_max_connections", null)?.toIntOrNull(),
        bufferSize = preferences.getString("byedpi_buffer_size", null)?.toIntOrNull(),
        defaultTtl = preferences.getString("byedpi_default_ttl", null)?.toIntOrNull(),
        noDomain = preferences.getBoolean("byedpi_no_domain", false),
        desyncHttp = preferences.getBoolean("byedpi_desync_http", true),
        desyncHttps = preferences.getBoolean("byedpi_desync_https", true),
        desyncUdp = preferences.getBoolean("byedpi_desync_udp", true),
        desyncMethod = preferences.getString("byedpi_desync_method", null)?.let { DesyncMethod.fromName(it) },
        splitPosition = preferences.getString("byedpi_split_position", null)?.toIntOrNull(),
        splitAtHost = preferences.getBoolean("byedpi_split_at_host", false),
        fakeTtl = preferences.getString("byedpi_fake_ttl", null)?.toIntOrNull(),
        fakeSni = preferences.getString("byedpi_fake_sni", null),
        oobChar = preferences.getString("byedpi_oob_data", null),
        hostMixedCase = preferences.getBoolean("byedpi_host_mixed_case", false),
        domainMixedCase = preferences.getBoolean("byedpi_domain_mixed_case", false),
        hostRemoveSpaces = preferences.getBoolean("byedpi_host_remove_spaces", false),
        tlsRecordSplit = preferences.getBoolean("byedpi_tlsrec_enabled", false),
        tlsRecordSplitPosition = preferences.getString("byedpi_tlsrec_position", null)?.toIntOrNull(),
        tlsRecordSplitAtSni = preferences.getBoolean("byedpi_tlsrec_at_sni", false),
        tcpFastOpen = preferences.getBoolean("byedpi_tcp_fast_open", false),
        udpFakeCount = preferences.getString("byedpi_udp_fake_count", null)?.toIntOrNull(),
        dropSack = preferences.getBoolean("byedpi_drop_sack", false),
        byedpiFakeOffset = preferences.getString("byedpi_fake_offset", null)?.toIntOrNull(),
        hostsMode = preferences.getString("byedpi_hosts_mode", null)?.let { HostsMode.fromName(it) },
        hosts = preferences.getString("byedpi_hosts_mode", null)?.let {
            when (HostsMode.fromName(it)) {
                HostsMode.Blacklist -> preferences.getString("byedpi_hosts_blacklist", null)
                HostsMode.Whitelist -> preferences.getString("byedpi_hosts_whitelist", null)
                else -> null
            }
        },
    )

    enum class DesyncMethod {
        None,
        Split,
        Disorder,
        Fake,
        OOB,
        DISOOB;

        companion object {
            fun fromName(name: String): DesyncMethod {
                return when (name) {
                    "none" -> None
                    "split" -> Split
                    "disorder" -> Disorder
                    "fake" -> Fake
                    "oob" -> OOB
                    "disoob" -> DISOOB
                    else -> throw IllegalArgumentException("Unknown desync method: $name")
                }
            }
        }
    }

    enum class HostsMode {
        Disable,
        Blacklist,
        Whitelist;

        companion object {
            fun fromName(name: String): HostsMode {
                return when (name) {
                    "disable" -> Disable
                    "blacklist" -> Blacklist
                    "whitelist" -> Whitelist
                    else -> throw IllegalArgumentException("Unknown hosts mode: $name")
                }
            }
        }
    }

    val uiargs: Array<String>
        get() {
            val args = mutableListOf("ciadpi")

            ip.takeIf { it.isNotEmpty() }?.let {
                args.add("-i${it}")
            }

            port.takeIf { it != 0 }?.let {
                args.add("-p${it}")
            }

            maxConnections.takeIf { it != 0 }?.let {
                args.add("-c${it}")
            }

            bufferSize.takeIf { it != 0 }?.let {
                args.add("-b${it}")
            }

            if (httpConnect) args.add("-G")

            val protocols = mutableListOf<String>()
            if (desyncHttps) protocols.add("t")
            if (desyncHttp) protocols.add("h")

            if (!hosts.isNullOrBlank()) {
                val hostStr = ":${hosts}"
                val hostBlock = mutableListOf<String>()

                when (hostsMode) {
                    HostsMode.Blacklist -> {
                        hostBlock.add("-H${hostStr}")
                        hostBlock.add("-An")
                        if (protocols.isNotEmpty()) {
                            hostBlock.add("-K${protocols.joinToString(",")}")
                        }
                    }
                    HostsMode.Whitelist -> {
                        if (protocols.isNotEmpty()) {
                            hostBlock.add("-K${protocols.joinToString(",")}")
                        }
                        hostBlock.add("-H${hostStr}")
                    }
                    else -> {}
                }
                args.addAll(hostBlock)
            } else {
                if (protocols.isNotEmpty()) {
                    args.add("-K${protocols.joinToString(",")}")
                }
            }

            defaultTtl.takeIf { it != 0 }?.let {
                args.add("-g${it}")
            }

            if (noDomain) {
                args.add("-N")
            }

            desyncMethod.let { method ->
                splitPosition.takeIf { it != 0 }?.let { pos ->
                    var posArg = pos.toString()
                    if (splitAtHost) {
                        posArg += "+h"
                    }
                    val option = when (method) {
                        DesyncMethod.Split -> "-s"
                        DesyncMethod.Disorder -> "-d"
                        DesyncMethod.OOB -> "-o"
                        DesyncMethod.DISOOB -> "-q"
                        DesyncMethod.Fake -> "-f"
                        DesyncMethod.None -> ""
                    }
                    args.add("${option}${posArg}")
                }
            }

            if (desyncMethod == DesyncMethod.Fake) {
                fakeTtl.takeIf { it != 0 }?.let {
                    args.add("-t${it}")
                }

                fakeSni.takeIf { it.isNotEmpty() }?.let {
                    args.add("-n${it}")
                }

                fakeOffset.takeIf { it != 0 }?.let {
                    args.add("-O${it}")
                }
            }

            if (desyncMethod == DesyncMethod.OOB ||
                desyncMethod == DesyncMethod.DISOOB) {
                args.add("-e${oobChar}")
            }

            val modHttpFlags = mutableListOf<String>()
            if (hostMixedCase) modHttpFlags.add("h")
            if (domainMixedCase) modHttpFlags.add("d")
            if (hostRemoveSpaces) modHttpFlags.add("r")
            if (modHttpFlags.isNotEmpty()) {
                args.add("-M${modHttpFlags.joinToString(",")}")
            }

            if (tlsRecordSplit) {
                tlsRecordSplitPosition.takeIf { it != 0 }?.let {
                    var tlsRecArg = it.toString()
                    if (tlsRecordSplitAtSni) {
                        tlsRecArg += "+s"
                    }
                    args.add("-r${tlsRecArg}")
                }
            }

            if (tcpFastOpen) {
                args.add("-F")
            }

            if (dropSack) {
                args.add("-Y")
            }

            args.add("-An")

            if (desyncUdp) {
                args.add("-Ku")

                udpFakeCount.takeIf { it != 0 }?.let {
                    args.add("-a${it}")
                }

                args.add("-An")
            }

            Log.d("ProxyPref", "UI to cmd: ${args.joinToString(" ")}")
            return args.toTypedArray()
        }
}
