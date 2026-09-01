# RoomDeck

![Android](https://img.shields.io/badge/Android-12%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-v1.0.1-67DED0)
![Visibility](https://img.shields.io/badge/repository-public-0B1F2C)

RoomDeck is an open-source multi-TV remote for TCL Android TVs. One Controller app on the phone manages multiple TVs, while a lightweight Receiver on each TV supplies its installed-app catalog and secure launch routes over Tailscale.

## Preview

<table width="100%">
  <tr>
    <td width="50%" align="center"><img src="docs/screenshots/controller-home.jpg" alt="RoomDeck controller home with saved TVs" width="100%"><br><sub>Controller home</sub></td>
    <td width="50%" align="center"><img src="docs/screenshots/add-tv.jpg" alt="RoomDeck Add a TV screen for Tailscale pairing" width="100%"><br><sub>Add a TV</sub></td>
  </tr>
  <tr>
    <td width="50%" align="center"><img src="docs/screenshots/remote-deck.jpg" alt="RoomDeck remote controls" width="100%"><br><sub>Remote deck</sub></td>
    <td width="50%" align="center"><img src="docs/screenshots/apps-grid.jpg" alt="RoomDeck installed apps grid" width="100%"><br><sub>TV apps</sub></td>
  </tr>
</table>

These privacy-safe captures show the V1 phone Controller during TCL Android TV testing.

## V1 features

- Save and switch between multiple TCL Android TVs.
- Use a compact portrait and landscape remote with Back, Home, Input, D-pad, volume, mute, channel, playback, and individual TV Off.
- Open and navigate the TV's own input chooser where the TV software supports the remote Input key.
- Browse each TV's launchable apps with progressively loaded icons.
- Open an app immediately and return to Remote for D-pad control.
- Hold and drag app tiles into a separate saved order for each TV.
- Send **All TVs Off**.

RoomDeck deliberately omits unreliable network power-on controls and its own USB browser. USB media stays inside the TV's Media Player app, preserving the player's normal browsing and subtitle support.

Compatibility note: the Input shortcut is model-specific on TCL software. If a TV does not respond to it, launch **Media Player** from the Apps tab instead.

## Architecture

- `mobile` — phone Controller (`io.github.tatselkrik.roomdeck`).
- `receiver` — Android TV Receiver (`io.github.tatselkrik.roomdeck.receiver`).
- `remote` — encrypted Android TV Remote v2 pairing and command channel.

Remote buttons use Android TV's encrypted remote-control protocol. Receiver reports launchable-app metadata first, then icons independently in small batches. Selected apps use one-time foreground launch routes, so listing or icon failures do not block ordinary remote controls.

RoomDeck requires Tailscale on the phone and every TV. It has no LAN fallback, cloud relay, RoomDeck account, analytics, advertising, or telemetry.

## Install

1. Download the Controller and Receiver APKs from the [latest GitHub release](https://github.com/tatselkrik/roomdeck/releases/latest).
2. Install **RoomDeck Receiver** on each TV and **RoomDeck Controller** on the phone.
3. Connect Tailscale on the phone and TVs to the same tailnet, with incoming connections enabled on each TV.
4. Open Receiver once on each TV and note its private Tailscale address.
5. In Controller, choose **Add TV**, enter a room name and the displayed address, then enter the Android TV Remote pairing code shown on the TV.

Receiver authorization completes through the successful Android TV Remote pairing; there is no second RoomDeck code.

## Build locally

Use a current Android Studio installation with its bundled JDK. The project compiles against Android API 36.

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug --no-daemon
```

Generated APKs are under `mobile/build/outputs/apk/debug/` and `receiver/build/outputs/apk/debug/`.

Those locally generated debug APKs are for development only. Public release APKs are built in release mode, signed with RoomDeck's production key, verified as non-debuggable, and published with SHA-256 checksums.

Maintainers can run `scripts\build-production-release.ps1` on Windows to create and verify the signed release artifacts. The helper asks for the signing password securely and never writes it into the repository.

## Release status

`v1.0.1` is the first public production release. It keeps the final V1 behavior from Controller test.18 and Receiver test.10 while replacing the private development packages with production-signed, non-debuggable APKs. The production APKs completed the Android TV 12/14 and cross-TV checklist. The tested Android TV 12 model did not respond to Input, and Media Player through Apps was verified as the working fallback. Automated tests, debug and release lint, debug and release assembly, APK metadata, signatures, checksums, the physical TV checklist, and exact-commit GitHub Actions are release gates.

See [`PRIVACY.md`](PRIVACY.md), [`SECURITY.md`](SECURITY.md), and the [`v1.0.1` release notes](docs/RELEASE_NOTES_v1.0.1.md).

## License and attribution

RoomDeck is licensed under Apache License 2.0. Parts of its Android TV Remote v2 implementation are adapted from [ScreenCast](https://github.com/ddagunts/ScreenCast) by Dmitry Dagunts. See [`NOTICE`](NOTICE) and [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).
