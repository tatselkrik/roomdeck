package io.github.tatselkrik.roomdeck.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import java.net.Inet4Address

object TailscaleRoute {
    fun isIpv4Address(value: String): Boolean {
        val parts = value.split('.')
        if (parts.size != 4) return false
        val octets = parts.map { part ->
            if (part.isEmpty() || part.length > 3 || part.any { !it.isDigit() }) return false
            part.toIntOrNull()?.takeIf { it in 0..255 } ?: return false
        }
        return octets[0] == 100 && octets[1] in 64..127
    }

    @Suppress("DEPRECATION")
    fun currentNetwork(context: Context): Network? {
        val manager = context.applicationContext.getSystemService(ConnectivityManager::class.java)
        return manager.allNetworks.firstOrNull { network ->
            val capabilities = manager.getNetworkCapabilities(network)
            val addresses = manager.getLinkProperties(network)?.linkAddresses.orEmpty()
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true &&
                addresses.any { link ->
                    val address = link.address
                    address is Inet4Address && isIpv4Address(address.hostAddress.orEmpty())
                }
        }
    }
}
