/*
 * Adapted for RoomDeck in 2026 from ScreenCast by Dmitry Dagunts.
 * Copyright 2026 Dmitry Dagunts. Licensed under Apache-2.0.
 * See NOTICE and THIRD_PARTY_NOTICES.md. This file contains modifications.
 */
package io.github.tatselkrik.roomdeck.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidTvPairingProtocolTest {
    @Test
    fun `pairing request matches canonical polo protobuf bytes`() {
        val encoded = OuterMessage.pairingRequest(
            PairingRequest(serviceName = "atvremote", clientName = "RoomDeck"),
        ).encode()

        assertEquals(
            "080210c80152150a0961747672656d6f74651208526f6f6d4465636b",
            encoded.toHex(),
        )
    }

    @Test
    fun `input-side options match working remote clients`() {
        val encoded = OuterMessage.options(
            Options(
                inputEncodings = listOf(Encoding(EncodingType.HEXADECIMAL, 6)),
                outputEncodings = emptyList(),
                preferredRole = RoleType.INPUT,
            ),
        ).encode()

        assertEquals("080210c801a201080a04080310061801", encoded.toHex())
    }

    @Test
    fun `configuration matches canonical polo protobuf bytes`() {
        val encoded = OuterMessage.configuration(
            Configuration(
                encoding = Encoding(EncodingType.HEXADECIMAL, 6),
                clientRole = RoleType.INPUT,
            ),
        ).encode()

        assertEquals("080210c801f201080a04080310061001", encoded.toHex())
    }

    private fun ByteArray.toHex(): String = joinToString("") { byte ->
        "%02x".format(byte.toInt() and 0xFF)
    }
}
