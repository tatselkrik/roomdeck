package io.github.tatselkrik.roomdeck.receiver.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class ReceiverPreferences(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val random = SecureRandom()

    val deviceId: String
        get() = preferences.getString(KEY_DEVICE_ID, null)
            ?: UUID.randomUUID().toString().also {
                preferences.edit().putString(KEY_DEVICE_ID, it).apply()
            }


    val isPaired: Boolean
        get() = bearerToken != null

    private val bearerToken: String?
        get() = preferences.getString(KEY_BEARER_TOKEN, null)


    @Synchronized
    fun authorizeController(): String = bearerToken ?: randomToken().also {
        preferences.edit().putString(KEY_BEARER_TOKEN, it).apply()
    }

    fun isAuthorized(token: String?): Boolean {
        val expected = bearerToken ?: return false
        return token != null && constantTimeEquals(expected, token)
    }

    @Synchronized
    fun resetPhoneAccess() {
        preferences.edit().remove(KEY_BEARER_TOKEN).apply()
    }

    private fun randomToken(): String {
        val bytes = ByteArray(32).also(random::nextBytes)
        return bytes.joinToString(separator = "") { "%02x".format(it.toInt() and 0xff) }
    }

    private fun constantTimeEquals(first: String, second: String): Boolean =
        MessageDigest.isEqual(first.toByteArray(Charsets.UTF_8), second.toByteArray(Charsets.UTF_8))

    private companion object {
        const val PREFERENCES_NAME = "roomdeck_receiver"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_BEARER_TOKEN = "bearer_token"
    }
}
