package io.github.tatselkrik.roomdeck.receiver.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restores the local Receiver after an ordinary TV reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            RoomDeckService.start(context)
        }
    }
}
