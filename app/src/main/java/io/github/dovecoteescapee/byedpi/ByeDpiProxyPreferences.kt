package io.github.dovecoteescapee.byedpi

class ByeDpiProxyPreferences(
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
    hostMixedCase: Boolean? = null,
    domainMixedCase: Boolean? = null,
    hostRemoveSpaces: Boolean? = null,
    tlsRecordSplit: Int? = null,
    tlsRecordSplitAtSni: Boolean? = null,
) {
    val port: Int = port ?: 1080
    val maxConnections: Int = maxConnections ?: 512
    val bufferSize: Int = bufferSize ?: 16384
    val defaultTtl: Int = defaultTtl ?: 0
    val noDomain: Boolean = noDomain ?: false
    val desyncKnown: Boolean = desyncKnown ?: false
    val desyncMethod: DesyncMethod = desyncMethod ?: DesyncMethod.None
    val splitPosition: Int = splitPosition ?: 3
    val splitAtHost: Boolean = splitAtHost ?: false
    val fakeTtl: Int = fakeTtl ?: 8
    val hostMixedCase: Boolean = hostMixedCase ?: false
    val domainMixedCase: Boolean = domainMixedCase ?: false
    val hostRemoveSpaces: Boolean = hostRemoveSpaces ?: false
    val tlsRecordSplit: Int = tlsRecordSplit ?: 0
    val tlsRecordSplitAtSni: Boolean = tlsRecordSplitAtSni ?: false

    enum class DesyncMethod {
        None,
        Split,
        Disorder,
        Fake;

        companion object {
            fun fromName(name: String): DesyncMethod {
                return when (name) {
                    "none" -> None
                    "split" -> Split
                    "disorder" -> Disorder
                    "fake" -> Fake
                    else -> throw IllegalArgumentException("Unknown desync method: $name")
                }
            }
        }
    }
}
