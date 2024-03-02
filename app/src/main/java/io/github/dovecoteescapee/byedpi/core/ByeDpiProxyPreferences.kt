package io.github.dovecoteescapee.byedpi.core

import android.content.SharedPreferences

class ByeDpiProxyPreferences(
    ip: String? = null,
    port: Int? = null,
    maxConnections: Int? = null,
    bufferSize: Int? = null,
    defaultTtl: Int? = null,
    noDomain: Boolean? = null,
    desyncKnown: Boolean? = null,
    desyncMethod: DesyncMethod? = null,
    splitPosition: Int? = null,
    splitAtHost: Boolean? = null,
    fakeTtl: Int? = null,
    fakeSni: String? = null,
    oobData: String? = null,
    hostMixedCase: Boolean? = null,
    domainMixedCase: Boolean? = null,
    hostRemoveSpaces: Boolean? = null,
    tlsRecordSplit: Boolean? = null,
    tlsRecordSplitPosition: Int? = null,
    tlsRecordSplitAtSni: Boolean? = null,
) {
    val ip: String = ip ?: "127.0.0.1"
    val port: Int = port ?: 1080
    val maxConnections: Int = maxConnections ?: 512
    val bufferSize: Int = bufferSize ?: 16384
    val defaultTtl: Int = defaultTtl ?: 0
    val noDomain: Boolean = noDomain ?: false
    val desyncKnown: Boolean = desyncKnown ?: false
    val desyncMethod: DesyncMethod = desyncMethod ?: DesyncMethod.Disorder
    val splitPosition: Int = splitPosition ?: 3
    val splitAtHost: Boolean = splitAtHost ?: false
    val fakeTtl: Int = fakeTtl ?: 8
    val fakeSni: String = fakeSni ?: "www.w3c.org"
    val oobData: String = oobData ?: "a"
    val hostMixedCase: Boolean = hostMixedCase ?: false
    val domainMixedCase: Boolean = domainMixedCase ?: false
    val hostRemoveSpaces: Boolean = hostRemoveSpaces ?: false
    val tlsRecordSplit: Boolean = tlsRecordSplit ?: false
    val tlsRecordSplitPosition: Int = tlsRecordSplitPosition ?: 0
    val tlsRecordSplitAtSni: Boolean = tlsRecordSplitAtSni ?: false

    constructor(preferences: SharedPreferences) : this(
        ip = preferences.getString("byedpi_proxy_ip", null),
        port = preferences.getString("byedpi_proxy_port", null)?.toIntOrNull(),
        maxConnections = preferences.getString("byedpi_max_connections", null)?.toIntOrNull(),
        bufferSize = preferences.getString("byedpi_buffer_size", null)?.toIntOrNull(),
        defaultTtl = preferences.getString("byedpi_default_ttl", null)?.toIntOrNull(),
        noDomain = preferences.getBoolean("byedpi_no_domain", false),
        desyncKnown = preferences.getBoolean("byedpi_desync_known", false),
        desyncMethod = preferences.getString("byedpi_desync_method", null)
            ?.let { DesyncMethod.fromName(it) },
        splitPosition = preferences.getString("byedpi_split_position", null)?.toIntOrNull(),
        splitAtHost = preferences.getBoolean("byedpi_split_at_host", false),
        fakeTtl = preferences.getString("byedpi_fake_ttl", null)?.toIntOrNull(),
        fakeSni = preferences.getString("byedpi_fake_sni", null),
        oobData = preferences.getString("byedpi_oob_data", null),
        hostMixedCase = preferences.getBoolean("byedpi_host_mixed_case", false),
        domainMixedCase = preferences.getBoolean("byedpi_domain_mixed_case", false),
        hostRemoveSpaces = preferences.getBoolean("byedpi_host_remove_spaces", false),
        tlsRecordSplit = preferences.getBoolean("byedpi_tlsrec_enabled", false),
        tlsRecordSplitPosition = preferences.getString("byedpi_tlsrec_position", null)?.toIntOrNull(),
        tlsRecordSplitAtSni = preferences.getBoolean("byedpi_tlsrec_at_sni", false),
        )

    enum class DesyncMethod {
        None,
        Split,
        Disorder,
        Fake,
        OOB;

        companion object {
            fun fromName(name: String): DesyncMethod {
                return when (name) {
                    "none" -> None
                    "split" -> Split
                    "disorder" -> Disorder
                    "fake" -> Fake
                    "oob" -> OOB
                    else -> throw IllegalArgumentException("Unknown desync method: $name")
                }
            }
        }
    }
}
