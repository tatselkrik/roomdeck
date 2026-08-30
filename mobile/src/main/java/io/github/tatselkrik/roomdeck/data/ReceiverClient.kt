package io.github.tatselkrik.roomdeck.data

import android.content.Context
import io.github.tatselkrik.roomdeck.remote.TailscaleRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom

data class ReceiverDevice(
    val deviceId: String,
    val model: String,
    val androidVersion: String,
    val receiverVersion: String,
    val paired: Boolean,
)

data class TvAppItem(
    val label: String,
    val packageName: String,
    val iconPngBase64: String?,
)


class ReceiverClient(
    context: Context,
    private val port: Int = DEFAULT_PORT,
) {
    private val appContext = context.applicationContext
    suspend fun device(host: String): ReceiverDevice {
        val json = request(host, "GET", "/v1/device")
        return ReceiverDevice(
            deviceId = json.getString("deviceId"),
            model = json.optString("model", "Android TV"),
            androidVersion = json.optString("androidVersion"),
            receiverVersion = json.optString("receiverVersion"),
            paired = json.optBoolean("paired"),
        )
    }


    fun newEnrollmentSecret(): String {
        val bytes = ByteArray(32).also(SecureRandom()::nextBytes)
        return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    suspend fun startEnrollment(host: String, secret: String): String =
        request(
            host,
            "POST",
            "/v1/enrollment/start",
            body = JSONObject().put("secret", secret),
        ).getString("launchUri")

    suspend fun completeEnrollment(host: String, secret: String): String =
        request(
            host,
            "POST",
            "/v1/enrollment/complete",
            body = JSONObject().put("secret", secret),
        ).getString("token")

    suspend fun apps(profile: TvProfile): List<TvAppItem> {
        val array = request(
            profile = profile,
            method = "GET",
            path = "/v1/apps",
            connectTimeoutMs = APP_LIST_CONNECT_TIMEOUT_MS,
            readTimeoutMs = APP_LIST_READ_TIMEOUT_MS,
        ).getJSONArray("apps")
        return buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    TvAppItem(
                        item.getString("label"),
                        item.getString("packageName"),
                        item.optString("iconPngBase64").takeIf(String::isNotBlank),
                    ),
                )
            }
        }
    }


    suspend fun appIcon(profile: TvProfile, packageName: String): String? {
        val encodedPackage = URLEncoder.encode(packageName, Charsets.UTF_8.name())
        return request(
            profile = profile,
            method = "GET",
            path = "/v1/apps/icon?packageName=$encodedPackage",
            connectTimeoutMs = ICON_CONNECT_TIMEOUT_MS,
            readTimeoutMs = ICON_READ_TIMEOUT_MS,
        ).optString("iconPngBase64").takeIf(String::isNotBlank)
    }

    suspend fun launch(profile: TvProfile, packageName: String): String =
        request(
            profile,
            "POST",
            "/v1/apps/launch",
            JSONObject().put("packageName", packageName),
        ).getString("launchUri")

    private suspend fun request(
        profile: TvProfile,
        method: String,
        path: String,
        body: JSONObject? = null,
        connectTimeoutMs: Int = CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = READ_TIMEOUT_MS,
    ): JSONObject {
        val token = requireNotNull(profile.receiverToken) { "Pair the RoomDeck Receiver first" }
        return request(
            host = profile.host,
            method = method,
            path = path,
            token = token,
            body = body,
            connectTimeoutMs = connectTimeoutMs,
            readTimeoutMs = readTimeoutMs,
        )
    }

    private suspend fun request(
        host: String,
        method: String,
        path: String,
        token: String? = null,
        body: JSONObject? = null,
        connectTimeoutMs: Int = CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = READ_TIMEOUT_MS,
    ): JSONObject = withContext(Dispatchers.IO) {
        val urlHost = if (':' in host && !host.startsWith('[')) "[$host]" else host
        val url = URL("http://$urlHost:$port$path")
        val network = TailscaleRoute.currentNetwork(appContext)
            ?: error("Tailscale is not connected. Open Tailscale and connect to your tailnet.")
        val connection = network.openConnection(url) as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Connection", "close")
            if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                val bytes = body.toString().toByteArray(Charsets.UTF_8)
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            val status = connection.responseCode
            val source = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = source?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = if (text.isBlank()) JSONObject() else JSONObject(text)
            if (status !in 200..299) {
                throw ReceiverException(status, json.optString("error", "Receiver request failed"))
            }
            json
        } finally {
            connection.disconnect()
        }
    }


    companion object {
        const val DEFAULT_PORT = 41_234
        private const val CONNECT_TIMEOUT_MS = 3_500
        private const val READ_TIMEOUT_MS = 7_000
        private const val APP_LIST_CONNECT_TIMEOUT_MS = 6_000
        private const val APP_LIST_READ_TIMEOUT_MS = 12_000
        private const val ICON_CONNECT_TIMEOUT_MS = 3_500
        private const val ICON_READ_TIMEOUT_MS = 5_000
    }
}

class ReceiverException(val statusCode: Int, message: String) : Exception(message)
