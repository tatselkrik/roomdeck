package io.github.tatselkrik.roomdeck.data

import io.github.tatselkrik.roomdeck.remote.AndroidTvDevice

data class TvProfile(
    val id: String,
    val roomName: String,
    val deviceName: String,
    val host: String,
    val remotePort: Int = 6466,
    val pairingPort: Int = 6467,
    val modelName: String? = null,
    val stableHardwareId: String? = null,
    val receiverToken: String? = null,
    val appOrder: List<String> = emptyList(),
) {
    fun asRemoteDevice() = AndroidTvDevice(
        name = deviceName,
        host = host,
        port = remotePort,
        pairingPort = pairingPort,
        modelName = modelName,
        stableId = stableHardwareId,
    )
}
