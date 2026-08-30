package io.github.tatselkrik.roomdeck.receiver.pairing

import java.util.concurrent.ConcurrentHashMap

/**
 * Short-lived proof that the controller which reached the Receiver can also control Android TV.
 * The secret is generated on the phone, approved only by an Android TV app-link launch, and is
 * consumed once when the phone requests its Receiver token.
 */
internal class EnrollmentTickets(
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    private val tickets = ConcurrentHashMap<String, Ticket>()

    fun start(secret: String): String? {
        removeExpired()
        if (!isValidSecret(secret)) return null
        tickets[secret] = Ticket(approved = false, expiresAtMs = nowMs() + VALIDITY_MS)
        return "roomdeck://enroll/$secret"
    }

    fun approve(secret: String): Boolean {
        removeExpired()
        var approved = false
        tickets.computeIfPresent(secret) { _, ticket ->
            approved = ticket.expiresAtMs >= nowMs()
            ticket.copy(approved = approved)
        }
        return approved
    }

    fun consumeApproved(secret: String): Boolean {
        val ticket = tickets.remove(secret) ?: return false
        return ticket.approved && ticket.expiresAtMs >= nowMs()
    }

    private fun removeExpired() {
        val now = nowMs()
        tickets.entries.removeIf { it.value.expiresAtMs < now }
    }

    private fun isValidSecret(secret: String): Boolean =
        secret.length == SECRET_HEX_LENGTH && secret.all { it in '0'..'9' || it in 'a'..'f' }

    private data class Ticket(val approved: Boolean, val expiresAtMs: Long)

    private companion object {
        const val VALIDITY_MS = 60_000L
        const val SECRET_HEX_LENGTH = 64
    }
}

internal object ReceiverEnrollment {
    private val tickets = EnrollmentTickets()

    fun start(secret: String): String? = tickets.start(secret)
    fun approve(secret: String): Boolean = tickets.approve(secret)
    fun consumeApproved(secret: String): Boolean = tickets.consumeApproved(secret)
}
