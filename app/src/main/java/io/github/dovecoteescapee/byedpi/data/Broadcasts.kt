package io.github.dovecoteescapee.byedpi.data

const val STARTED_BROADCAST = "io.github.dovecoteescapee.byedpi.STARTED"
const val STOPPED_BROADCAST = "io.github.dovecoteescapee.byedpi.STOPPED"
const val FAILED_BROADCAST = "io.github.dovecoteescapee.byedpi.FAILED"

const val SENDER = "sender"

enum class Sender(val senderName: String) {
    Proxy("Proxy"),
    VPN("VPN")
}
