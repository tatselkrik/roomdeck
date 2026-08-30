# Third-party notices

## ScreenCast

RoomDeck's Android TV Remote v2 implementation and protocol tests contain modified code adapted from [ScreenCast](https://github.com/ddagunts/ScreenCast).

Copyright 2026 Dmitry Dagunts
License: Apache License 2.0

Modified files carry an adaptation notice. RoomDeck changed package names, logging, user-facing device identity, supported remote keys, module boundaries, and integration behavior. The current build does not include the adapted mDNS discovery layer; TVs are added by entering the Receiver-displayed Tailscale address.

## AndroidX, Kotlin, and kotlinx.coroutines

RoomDeck uses AndroidX libraries, Kotlin, and kotlinx.coroutines under their respective open-source licenses. Dependency metadata in `gradle/libs.versions.toml` identifies the versions used by this build.
