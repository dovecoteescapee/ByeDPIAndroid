package io.github.dovecoteescapee.byedpi.data

data class AppSettings(
    val app: String,
    val version: String,
    val history: List<Command>,
    val apps: List<String>,
    val settings: Map<String, Any?>
)