# RoomDeck v1.0.0

RoomDeck v1.0.0 is the first private personal release for controlling multiple TCL Android TVs through Tailscale.

## Release baseline

- Controller behavior promoted from `1.0.0-test.18`.
- Receiver behavior promoted from `1.0.0-test.10`.
- Final package versions are `1.0.0`; version codes are incremented so they install over the test builds.

## Highlights

- Compact portrait and landscape Remote layouts.
- Individual TV Off and **All TVs Off**.
- TV-native input selection and navigation.
- Per-TV installed-app catalogs with metadata-first loading and independent icon batches.
- Immediate selected-app launching without rebuilding the complete icon catalog.
- Per-TV hold-and-drag app ordering.
- Tailscale-only Receiver transport with per-TV authorization.
- TV-native Media Player workflow for USB browsing, playback, and subtitles.

## Intentional limitations

- No individual or group TV power-on control.
- No RoomDeck USB browser or internal media player.
- No automatic Receiver discovery.
- No direct-LAN fallback, cloud relay, accounts, analytics, advertising, or automation scenes.

## Verification

The release gate runs unit tests, Android lint, and debug APK assembly. Package metadata, APK signatures, and SHA-256 manifests are verified locally before assets are uploaded. GitHub Actions must pass for the exact release commit before the `v1.0.0` tag and GitHub release are published.

Android TV 14 behavior has been exercised on the primary TCL. Android TV 12 and the complete cross-TV checks remain recorded as pending in `docs/TV_TEST_CHECKLIST.md`; this release must not be described as fully qualified across both TVs until that checklist is completed.

## Assets

- `RoomDeck-Controller-v1.0.0.apk`
- `RoomDeck-Receiver-v1.0.0.apk`
- `SHA256SUMS-v1.0.0.txt`

These private personal APKs retain the existing development signing identity so they can update the installed test builds without clearing RoomDeck pairing data. They are debuggable development packages intended only for Kirk's private devices; do not redistribute them as production-signed binaries.
