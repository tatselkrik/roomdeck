package io.github.tatselkrik.roomdeck.receiver.network

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkAddress {
    fun tailscaleIpv4(): String? = runCatching {
        NetworkInterface.getNetworkInterfaces().toList()
            .asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .mapNotNull { it.hostAddress }
            .firstOrNull(::isTailscaleIpv4)
    }.getOrNull()

    fun isTailscaleIpv4(value: String): Boolean {
        val parts = value.split('.')
        if (parts.size != 4) return false
        val octets = parts.map { part ->
            if (part.isEmpty() || part.length > 3 || part.any { !it.isDigit() }) return false
            part.toIntOrNull()?.takeIf { it in 0..255 } ?: return false
        }
        return octets[0] == 100 && octets[1] in 64..127
    }
}
