package io.github.tatselkrik.roomdeck.receiver.service

import io.github.tatselkrik.roomdeck.receiver.server.RoomDeckServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReceiverStatus(
    val running: Boolean = false,
    val address: String? = null,
    val port: Int = RoomDeckServer.DEFAULT_PORT,
    val error: String? = null,
)

object ReceiverRuntime {
    private val mutableStatus = MutableStateFlow(ReceiverStatus())
    val status = mutableStatus.asStateFlow()

    fun update(value: ReceiverStatus) {
        mutableStatus.value = value
    }
}
