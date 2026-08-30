package io.github.tatselkrik.roomdeck.receiver.server

import android.content.Context

import android.os.Build
import io.github.tatselkrik.roomdeck.receiver.BuildConfig
import io.github.tatselkrik.roomdeck.receiver.apps.AppCatalog
import io.github.tatselkrik.roomdeck.receiver.apps.AppLaunchTarget
import io.github.tatselkrik.roomdeck.receiver.apps.AppLaunchTickets
import io.github.tatselkrik.roomdeck.receiver.data.ReceiverPreferences
import io.github.tatselkrik.roomdeck.receiver.pairing.ReceiverEnrollment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.URLDecoder

class RoomDeckServer(
    context: Context,
    private val bindAddress: InetAddress,
    private val port: Int = DEFAULT_PORT,
) {
    private val appContext = context.applicationContext
    private val preferences = ReceiverPreferences(appContext)
    private val appCatalog = AppCatalog(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null

    fun start() {
        if (acceptJob != null) return
        val socket = ServerSocket().also {
            it.reuseAddress = true
            it.bind(InetSocketAddress(bindAddress, port))
        }
        serverSocket = socket
        acceptJob = scope.launch {
            while (isActive) {
                val client = runCatching { socket.accept() }.getOrNull() ?: break
                launch { client.use(::handleClient) }
            }
        }
    }

    fun stop() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        acceptJob?.cancel()
        acceptJob = null
        scope.cancel()
    }

    private fun handleClient(socket: Socket) {
        socket.soTimeout = SOCKET_TIMEOUT_MS
        val output = BufferedOutputStream(socket.getOutputStream())
        val request = runCatching { readRequest(BufferedInputStream(socket.getInputStream())) }
            .getOrElse {
                writeJson(output, 400, JSONObject().put("error", "Malformed request"))
                return
            }

        val response = runCatching { route(request) }
            .getOrElse { error ->
                ApiResponse(
                    500,
                    JSONObject().put("error", error.message ?: "Receiver error"),
                )
            }
        writeJson(output, response.status, response.body)
    }

    private fun route(request: HttpRequest): ApiResponse {
        if (request.method == "GET" && request.path == "/v1/device") {
            return ok(deviceJson())
        }

        if (request.method == "POST" && request.path == "/v1/enrollment/start") {
            val secret = request.jsonBody().optString("secret")
            val launchUri = ReceiverEnrollment.start(secret)
                ?: return ApiResponse(400, JSONObject().put("error", "Invalid enrollment request"))
            return ok(JSONObject().put("launchUri", launchUri))
        }
        if (request.method == "POST" && request.path == "/v1/enrollment/complete") {
            val secret = request.jsonBody().optString("secret")
            if (!ReceiverEnrollment.consumeApproved(secret)) {
                return ApiResponse(409, JSONObject().put("error", "Waiting for Android TV authorization"))
            }
            return ok(
                deviceJson()
                    .put("token", preferences.authorizeController())
                    .put("paired", true),
            )
        }

        val token = request.headers["authorization"]
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
        if (!preferences.isAuthorized(token)) {
            return ApiResponse(401, JSONObject().put("error", "Receiver pairing required"))
        }

        return when {
            request.method == "GET" && request.path == "/v1/apps" -> {
                val apps = JSONArray()
                appCatalog.list().forEach { app ->
                    apps.put(
                        JSONObject()
                            .put("label", app.label)
                            .put("packageName", app.packageName),
                    )
                }
                ok(JSONObject().put("apps", apps))
            }

            request.method == "GET" && request.path == "/v1/apps/icon" -> {
                val packageName = request.query["packageName"].orEmpty()
                if (packageName.isBlank()) {
                    return ApiResponse(400, JSONObject().put("error", "Package name is required"))
                }
                val icon = appCatalog.icon(packageName)
                    ?: return ApiResponse(404, JSONObject().put("error", "App icon is unavailable"))
                ok(JSONObject().put("iconPngBase64", icon))
            }

            request.method == "POST" && request.path == "/v1/apps/launch" -> {
                val app = appCatalog.find(request.jsonBody().optString("packageName"))
                    ?: return ApiResponse(404, JSONObject().put("error", "App is no longer available"))
                val launchUri = AppLaunchTickets.issue(AppLaunchTarget(app.packageName, app.activityName))
                ok(JSONObject().put("launchUri", launchUri))
            }
            else -> ApiResponse(404, JSONObject().put("error", "Unknown RoomDeck endpoint"))
        }
    }

    private fun deviceJson(): JSONObject = JSONObject()
        .put("deviceId", preferences.deviceId)
        .put("name", Build.MODEL.ifBlank { "Android TV" })
        .put("manufacturer", Build.MANUFACTURER)
        .put("model", Build.MODEL)
        .put("androidVersion", Build.VERSION.RELEASE)
        .put("sdk", Build.VERSION.SDK_INT)
        .put("receiverVersion", BuildConfig.VERSION_NAME)
        .put("paired", preferences.isPaired)

    private fun readRequest(input: BufferedInputStream): HttpRequest {
        val requestLine = readAsciiLine(input, MAX_HEADER_LINE)
        val parts = requestLine.split(' ', limit = 3)
        require(parts.size == 3) { "Invalid request line" }
        val method = parts[0].uppercase()
        require(method == "GET" || method == "POST") { "Unsupported method" }
        val target = parts[1]

        val headers = linkedMapOf<String, String>()
        while (true) {
            val line = readAsciiLine(input, MAX_HEADER_LINE)
            if (line.isEmpty()) break
            val separator = line.indexOf(':')
            require(separator > 0) { "Invalid header" }
            headers[line.substring(0, separator).trim().lowercase()] =
                line.substring(separator + 1).trim()
        }
        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
        require(contentLength in 0..MAX_BODY_SIZE) { "Invalid body size" }
        val body = ByteArray(contentLength)
        var offset = 0
        while (offset < contentLength) {
            val read = input.read(body, offset, contentLength - offset)
            require(read >= 0) { "Unexpected end of body" }
            offset += read
        }

        val uri = URI("http://roomdeck$target")
        val query = uri.rawQuery.orEmpty()
            .split('&')
            .filter(String::isNotBlank)
            .associate { pair ->
                val pieces = pair.split('=', limit = 2)
                decode(pieces[0]) to decode(pieces.getOrElse(1) { "" })
            }
        return HttpRequest(
            method = method,
            path = uri.path,
            query = query,
            headers = headers,
            body = String(body, Charsets.UTF_8),
        )
    }

    private fun readAsciiLine(input: BufferedInputStream, maxBytes: Int): String {
        val output = ByteArrayOutputStream()
        while (output.size() <= maxBytes) {
            val value = input.read()
            require(value >= 0) { "Unexpected end of headers" }
            if (value == '\n'.code) {
                val bytes = output.toByteArray()
                val length = if (bytes.lastOrNull() == '\r'.code.toByte()) bytes.size - 1 else bytes.size
                return String(bytes, 0, length, Charsets.US_ASCII)
            }
            output.write(value)
        }
        error("Header line too long")
    }

    private fun writeJson(output: BufferedOutputStream, status: Int, json: JSONObject) {
        val body = json.toString().toByteArray(Charsets.UTF_8)
        val reason = when (status) {
            200 -> "OK"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            404 -> "Not Found"
            409 -> "Conflict"
            429 -> "Too Many Requests"
            else -> "Internal Server Error"
        }
        val headers = buildString {
            append("HTTP/1.1 $status $reason\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n")
            append("Content-Length: ${body.size}\r\n")
            append("Cache-Control: no-store\r\n")
            append("Connection: close\r\n\r\n")
        }.toByteArray(Charsets.US_ASCII)
        output.write(headers)
        output.write(body)
        output.flush()
    }

    private fun ok(json: JSONObject) = ApiResponse(200, json)
    private fun decode(value: String): String = URLDecoder.decode(value, Charsets.UTF_8.name())

    private data class HttpRequest(
        val method: String,
        val path: String,
        val query: Map<String, String>,
        val headers: Map<String, String>,
        val body: String,
    ) {
        fun jsonBody(): JSONObject = if (body.isBlank()) JSONObject() else JSONObject(body)
    }

    private data class ApiResponse(val status: Int, val body: JSONObject)

    companion object {
        const val DEFAULT_PORT = 41_234
        private const val SOCKET_TIMEOUT_MS = 5_000
        private const val MAX_HEADER_LINE = 8_192
        private const val MAX_BODY_SIZE = 65_536
    }
}
