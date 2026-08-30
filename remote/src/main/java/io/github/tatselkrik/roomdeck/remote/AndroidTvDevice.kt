/*
 * Adapted for RoomDeck in 2026 from ScreenCast by Dmitry Dagunts.
 * Copyright 2026 Dmitry Dagunts. Licensed under Apache-2.0.
 * See NOTICE and THIRD_PARTY_NOTICES.md. This file contains modifications.
 */
package io.github.tatselkrik.roomdeck.remote

// Identifies an Android TV / Google TV target found through either the
// `_androidtvremote2._tcp` service or an installed RoomDeck Receiver.
// The remote-control port is normally 6466 and the pairing port is 6467.
//
// Android TV's `bt`/`bs` TXT record supplies bleMac on firmwares that expose it.
// stableId may instead come from the per-TV RoomDeck Receiver, allowing the same
// TV profile to survive DHCP address changes even when the remote TXT record is absent.
data class AndroidTvDevice(
    val name: String,
    val host: String,
    val port: Int = 6466,
    val pairingPort: Int = 6467,
    val modelName: String? = null,
    val bleMac: String? = null,
    val stableId: String? = bleMac,
)
