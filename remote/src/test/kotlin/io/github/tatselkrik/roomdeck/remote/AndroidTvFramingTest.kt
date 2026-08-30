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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class AndroidTvFramingTest {
    @Test
    fun `frame under 128 bytes uses one-byte prefix`() {
        val payload = byteArrayOf(1, 2, 3, 4, 5)
        val output = ByteArrayOutputStream()
        AndroidTvFraming.writeFrame(output, payload)

        assertEquals(5.toByte(), output.toByteArray()[0])
        assertEquals(payload.size + 1, output.size())
    }

    @Test
    fun `frame over 127 bytes uses multi-byte prefix`() {
        val payload = ByteArray(200) { it.toByte() }
        val output = ByteArrayOutputStream()
        AndroidTvFraming.writeFrame(output, payload)

        val framed = output.toByteArray()
        assertEquals(0xC8.toByte(), framed[0])
        assertEquals(0x01.toByte(), framed[1])
        assertEquals(payload.size + 2, framed.size)
    }

    @Test
    fun `framing round trip preserves payload`() {
        val payload = ByteArray(1_024) { ((it * 31) % 256).toByte() }
        val output = ByteArrayOutputStream()
        AndroidTvFraming.writeFrame(output, payload)

        assertArrayEquals(
            payload,
            AndroidTvFraming.readFrame(ByteArrayInputStream(output.toByteArray())),
        )
    }

    @Test
    fun `truncated payload is rejected`() {
        val truncated = byteArrayOf(5, 1, 2)

        assertThrows(IOException::class.java) {
            AndroidTvFraming.readFrame(ByteArrayInputStream(truncated))
        }
    }

    @Test
    fun `oversized frame is rejected`() {
        val output = ByteArrayOutputStream()
        AndroidTvFraming.writeVarint(output, 100_000L)

        assertThrows(IllegalStateException::class.java) {
            AndroidTvFraming.readFrame(ByteArrayInputStream(output.toByteArray()))
        }
    }
}
