package io.github.tatselkrik.roomdeck.receiver.apps

import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

internal data class AppLaunchTarget(
    val packageName: String,
    val activityName: String,
)

internal object AppLaunchTickets {
    private val random = SecureRandom()
    private val targets = ConcurrentHashMap<String, Ticket>()

    fun issue(target: AppLaunchTarget): String {
        removeExpired()
        val tokenBytes = ByteArray(24).also(random::nextBytes)
        val token = tokenBytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
        targets[token] = Ticket(target, System.currentTimeMillis() + VALIDITY_MS)
        return "roomdeck://app/$token"
    }

    fun consume(token: String): AppLaunchTarget? {
        val ticket = targets.remove(token) ?: return null
        return ticket.target.takeIf { ticket.expiresAtMs >= System.currentTimeMillis() }
    }

    private fun removeExpired() {
        val now = System.currentTimeMillis()
        targets.entries.removeIf { it.value.expiresAtMs < now }
    }

    private data class Ticket(val target: AppLaunchTarget, val expiresAtMs: Long)

    private const val VALIDITY_MS = 60_000L
}
