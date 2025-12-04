package io.github.dovecoteescapee.byedpi.data

data class Command(
    var text: String,
    var pinned: Boolean = false,
    var name: String? = null
)