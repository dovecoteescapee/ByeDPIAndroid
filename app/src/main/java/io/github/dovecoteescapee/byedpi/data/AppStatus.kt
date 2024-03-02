package io.github.dovecoteescapee.byedpi.data

enum class AppStatus {
    Halted,
    Running,
}

enum class Mode {
    Proxy,
    VPN;

    companion object {
        fun fromSender(sender: Sender): Mode = when (sender) {
            Sender.Proxy -> Proxy
            Sender.VPN -> VPN
        }

        fun fromString(name: String): Mode = when (name) {
            "proxy" -> Proxy
            "vpn" -> VPN
            else -> throw IllegalArgumentException("Invalid mode: $name")
        }
    }
}