package io.github.tatselkrik.roomdeck.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TailscaleRouteTest {
    @Test
    fun `accepts the complete Tailscale IPv4 range`() {
        assertTrue(TailscaleRoute.isIpv4Address("100.64.0.1"))
        assertTrue(TailscaleRoute.isIpv4Address("100.100.20.30"))
        assertTrue(TailscaleRoute.isIpv4Address("100.127.255.254"))
    }

    @Test
    fun `rejects LAN public and malformed addresses`() {
        assertFalse(TailscaleRoute.isIpv4Address("100.63.255.255"))
        assertFalse(TailscaleRoute.isIpv4Address("100.128.0.1"))
        assertFalse(TailscaleRoute.isIpv4Address("192.168.1.5"))
        assertFalse(TailscaleRoute.isIpv4Address("bedroom-tv"))
        assertFalse(TailscaleRoute.isIpv4Address("100.64.1.999"))
    }
}
