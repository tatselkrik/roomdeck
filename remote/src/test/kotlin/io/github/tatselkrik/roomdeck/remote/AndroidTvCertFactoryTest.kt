/*
 * Adapted for RoomDeck in 2026 from ScreenCast by Dmitry Dagunts.
 * Copyright 2026 Dmitry Dagunts. Licensed under Apache-2.0.
 * See NOTICE and THIRD_PARTY_NOTICES.md. This file contains modifications.
 */
package io.github.tatselkrik.roomdeck.remote

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class AndroidTvCertFactoryTest {
    @Test
    fun `pairing identity is a valid self-signed RSA v1 certificate`() {
        val now = System.currentTimeMillis()
        val material = AndroidTvCertFactory.generate(now)

        assertEquals(1, material.cert.version)
        assertEquals("RSA", material.cert.publicKey.algorithm)
        material.cert.checkValidity(Date(now))
        material.cert.verify(material.cert.publicKey)
    }
}
