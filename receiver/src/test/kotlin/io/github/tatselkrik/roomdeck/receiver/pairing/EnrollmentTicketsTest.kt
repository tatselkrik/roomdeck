package io.github.tatselkrik.roomdeck.receiver.pairing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnrollmentTicketsTest {
    @Test
    fun approvalIsRequiredAndCanOnlyBeConsumedOnce() {
        var now = 1_000L
        val tickets = EnrollmentTickets { now }
        val secret = "ab".repeat(32)

        assertNotNull(tickets.start(secret))
        assertFalse(tickets.consumeApproved(secret))

        assertNotNull(tickets.start(secret))
        assertTrue(tickets.approve(secret))
        assertTrue(tickets.consumeApproved(secret))
        assertFalse(tickets.consumeApproved(secret))

        now += 61_000L
        assertNotNull(tickets.start(secret))
        now += 61_000L
        assertFalse(tickets.approve(secret))
    }

    @Test
    fun malformedSecretsAreRejected() {
        val tickets = EnrollmentTickets()
        assertNull(tickets.start("123456"))
        assertNull(tickets.start("G".repeat(64)))
    }
}
