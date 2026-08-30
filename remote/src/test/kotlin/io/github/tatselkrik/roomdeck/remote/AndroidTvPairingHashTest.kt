/*
 * Adapted for RoomDeck in 2026 from ScreenCast by Dmitry Dagunts.
 * Copyright 2026 Dmitry Dagunts. Licensed under Apache-2.0.
 * See NOTICE and THIRD_PARTY_NOTICES.md. This file contains modifications.
 */
package io.github.tatselkrik.roomdeck.remote

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey

class AndroidTvPairingHashTest {
    @Test
    fun `nonce drops pairing-code prefix`() {
        assertArrayEquals(
            byteArrayOf(0x12, 0x34),
            AndroidTvPairingHash.extractNonce("F01234"),
        )
    }

    @Test
    fun `pairing code must contain exactly six hex characters`() {
        assertThrows(IllegalArgumentException::class.java) {
            AndroidTvPairingHash.extractNonce("F0ABC")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidTvPairingHash.extractNonce("F012345")
        }
    }

    @Test
    fun `checksum prefix must match first digest byte`() {
        val digest = byteArrayOf(0xA4.toByte(), 1, 2, 3)

        assertEquals(true, AndroidTvPairingHash.checksumMatches(digest, "A41234"))
        assertEquals(false, AndroidTvPairingHash.checksumMatches(digest, "A51234"))
    }

    @Test
    fun `invalid pairing codes are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AndroidTvPairingHash.extractNonce("F0ZZZZ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidTvPairingHash.extractNonce("F")
        }
    }

    @Test
    fun `unsigned RSA integers discard only a sign byte`() {
        with(AndroidTvPairingHash) {
            val value = BigInteger("9876543210ABCDEF", 16).toUnsignedBytes()
            assertEquals(8, value.size)
            assertEquals(0x98.toByte(), value[0])
            assertArrayEquals(byteArrayOf(0), BigInteger.ZERO.toUnsignedBytes())
        }
    }

    @Test
    fun `pairing digest depends on code and endpoint order`() {
        val generator = KeyPairGenerator.getInstance("RSA").apply { initialize(2_048) }
        val client = generator.generateKeyPair().public as RSAPublicKey
        val server = generator.generateKeyPair().public as RSAPublicKey

        val first = AndroidTvPairingHash.compute(client, server, "F01234")
        val differentCode = AndroidTvPairingHash.compute(client, server, "F01235")
        val reversed = AndroidTvPairingHash.compute(server, client, "F01234")

        assertEquals(false, first.contentEquals(differentCode))
        assertEquals(false, first.contentEquals(reversed))
    }
}
