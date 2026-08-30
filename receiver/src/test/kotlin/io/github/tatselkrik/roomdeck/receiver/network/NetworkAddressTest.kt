package io.github.tatselkrik.roomdeck.receiver.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkAddressTest {
    @Test
    fun `recognizes only Tailscale IPv4 addresses`() {
        assertTrue(NetworkAddress.isTailscaleIpv4("100.64.0.1"))
        assertTrue(NetworkAddress.isTailscaleIpv4("100.127.255.254"))
        assertFalse(NetworkAddress.isTailscaleIpv4("100.63.255.255"))
        assertFalse(NetworkAddress.isTailscaleIpv4("100.128.0.1"))
        assertFalse(NetworkAddress.isTailscaleIpv4("192.168.1.2"))
        assertFalse(NetworkAddress.isTailscaleIpv4("not-an-address"))
    }
}
