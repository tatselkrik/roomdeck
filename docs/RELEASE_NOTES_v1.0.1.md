# RoomDeck v1.0.1

RoomDeck v1.0.1 is the first public production release for controlling multiple TCL Android TVs through Tailscale.

## What changed from v1.0.0

The V1 product behavior is unchanged. This release replaces the private physical-test packaging with public distribution packaging:

- Controller version code `20` and Receiver version code `12`.
- Release-mode, non-debuggable APKs.
- Code and resource shrinking enabled for both applications.
- Both APKs signed with the same permanent RoomDeck production identity.
- SHA-256 checksums published beside the APKs.
- CI covers tests, debug and release lint, and debug and release assembly.

The private `v1.0.0` APKs used a development signing identity and are not redistributed. Android will not install `v1.0.1` over those packages because the signing identities differ. Uninstall the development builds first, then install `v1.0.1` and pair the TVs again.

## V1 features

- Save and switch between multiple TCL Android TVs.
- Use ordinary remote controls, with the TV's native input chooser where the model supports the Input key.
- Browse, launch, and reorder each TV's installed apps independently.
- Turn off one TV or use **All TVs Off**.
- Communicate only through Tailscale, with per-TV Receiver authorization.

RoomDeck intentionally excludes network power-on, a RoomDeck USB browser, phone-media casting, playback transfer, cloud services, accounts, analytics, advertising, and automation scenes.

## Requirements

- Android 12 or newer on the Controller phone and Android TVs.
- Tailscale installed and connected to the same tailnet on every device.
- Sideloading permission for the app used to open each APK.

## Release verification

The production APKs passed unit tests, debug and release lint, debug and release assembly, signature verification, non-debuggable manifest inspection, checksum generation, privacy review, and the Android TV 12/14 physical checklist. The tested Android TV 12 model did not respond to the Input key; its Media Player was verified as the working route from Apps, so this is an accepted model-specific compatibility limitation. Exact-commit GitHub Actions remains required before publication.

## Assets

- `RoomDeck-Controller-v1.0.1.apk`
- `RoomDeck-Receiver-v1.0.1.apk`
- `SHA256SUMS-v1.0.1.txt`
