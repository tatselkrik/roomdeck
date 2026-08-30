package io.github.tatselkrik.roomdeck.receiver.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.tatselkrik.roomdeck.receiver.MainActivity
import io.github.tatselkrik.roomdeck.receiver.R
import io.github.tatselkrik.roomdeck.receiver.network.NetworkAddress
import io.github.tatselkrik.roomdeck.receiver.server.RoomDeckServer
import java.net.InetAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RoomDeckService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var server: RoomDeckServer? = null
    private var monitorJob: Job? = null
    private var boundAddress: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
        )
        startReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESTART) {
            stopReceiver()
            startReceiver()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopReceiver()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startReceiver() {
        stopReceiver()
        ReceiverRuntime.update(ReceiverStatus())
        monitorJob = serviceScope.launch {
            while (isActive) {
                refreshTailscaleBinding()
                delay(TAILSCALE_POLL_MS)
            }
        }
    }

    private fun refreshTailscaleBinding() {
        val address = NetworkAddress.tailscaleIpv4()
        if (address == boundAddress && (address == null || server != null)) return

        server?.stop()
        server = null
        boundAddress = null

        if (address == null) {
            ReceiverRuntime.update(ReceiverStatus())
            return
        }

        runCatching {
            val addressObject = InetAddress.getByName(address)
            server = RoomDeckServer(this, addressObject).also { it.start() }

            boundAddress = address
            ReceiverRuntime.update(
                ReceiverStatus(
                    running = true,
                    address = address,
                    port = RoomDeckServer.DEFAULT_PORT,
                ),
            )
        }.onFailure { error ->
            ReceiverRuntime.update(
                ReceiverStatus(error = error.message ?: "Unable to bind Receiver to Tailscale"),
            )
        }
    }

    private fun stopReceiver() {
        monitorJob?.cancel()
        monitorJob = null
        server?.stop()
        server = null
        boundAddress = null
        ReceiverRuntime.update(ReceiverStatus())
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_roomdeck_foreground)
        .setContentTitle(getString(R.string.service_notification_title))
        .setContentText(getString(R.string.service_notification_text))
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        .build()

    companion object {
        private const val CHANNEL_ID = "roomdeck_receiver"
        private const val NOTIFICATION_ID = 4_123
        private const val TAILSCALE_POLL_MS = 2_000L
        private const val ACTION_RESTART = "io.github.tatselkrik.roomdeck.receiver.RESTART"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, RoomDeckService::class.java))
        }

        fun restart(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, RoomDeckService::class.java).setAction(ACTION_RESTART),
            )
        }
    }
}
