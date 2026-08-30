# Changelog

## 1.0.0 — 2026-08-30

First private personal release.

- Promotes Controller test.18 and Receiver test.10 behavior to final `1.0.0` package metadata.
- Increments package version codes so V1 installs over the final physical-test builds without clearing pairing data.
- Adds privacy-safe README screenshot placeholders, private-release notes, and a documented GitHub release asset contract.
- Retains Tailscale-only transport, responsive per-TV app catalogs, TV-native Media Player access, and the final compact Remote layouts.
- Keeps Android TV 12 and the complete cross-TV checklist explicitly pending rather than claiming unverified compatibility.

GitHub Actions must pass for the exact release commit before tagging and publishing `v1.0.0`.

## 1.0.0-test.18 / Receiver 1.0.0-test.10 — 2026-08-30

Final landscape vertical-justification candidate.

- Anchors **Remove this TV** at the landscape content's bottom edge.
- Divides the remaining height equally between tabs and controls, controls and playback, and playback and the remove action.
- Leaves portrait layout, control sizes, power-off behavior, and responsive app loading unchanged.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.17 / Receiver 1.0.0-test.10 — 2026-08-30

Landscape remove-action clipping repair.

- Increases the landscape **Remove this TV** control from its clipping `34dp` box to `44dp`.
- Removes vertical content padding inside that compact control so its icon and label render fully.
- Retains test.16's compact fixed-height landscape sequence and responsive app-catalog improvements unchanged.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.16 / Receiver 1.0.0-test.10 — 2026-08-30

Final landscape-spacing and responsive app-catalog candidate.

- Removes the weighted blank space around landscape controls so tabs, controls, playback, and **Remove this TV** use small consistent gaps and the remove action stays visible.
- Returns installed-app names immediately without waiting for Receiver to encode every icon.
- Loads each app icon independently in small batches; a failed icon keeps its letter placeholder without hiding or blocking the other apps.
- Launches only the selected package without rebuilding the full icon catalog, so tiles remain usable while icons are still arriving.
- Gives the lightweight app-metadata request a longer targeted timeout while keeping ordinary remote and launch requests bounded.
- Retains the physically confirmed TCL power-off repair.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.15 / Receiver 1.0.0-test.9 — 2026-08-30

Landscape sizing and footer-visibility candidate.

- Uses one smaller shared landscape size for Back/Home/Input, Volume, D-pad, and Channel controls.
- Constrains Remote and Apps content to the height remaining below the detail header and tabs, preventing **Remove this TV** from being clipped.
- Includes test.14's true equal portrait spacing and retains the physically confirmed TCL power-off repair.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.14 / Receiver 1.0.0-test.9 — 2026-08-30

Portrait equal-spacing candidate.

- Replaced hand-tuned portrait gaps with true equal vertical distribution across the five control sections.
- Keeps Back/Home/Input at the top and **Remove this TV** at the bottom while giving equal space between D-pad, Volume/Channel, and Playback.
- Retains the approved control sizes, landscape layout, and physically confirmed TCL power-off repair unchanged.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.13 / Receiver 1.0.0-test.9 — 2026-08-30

Final screenshot-driven Controller layout candidate.

- Removed the unnecessary `38dp` top gap from the TV detail screen in portrait and landscape.
- Explicitly centered the portrait D-pad, moved Volume and Channel slightly lower, and moved the playback strip upward for balanced spacing.
- Anchored **Remove this TV** below the flexible space so it stays at the bottom in portrait and becomes fully visible in landscape.
- Retained the test.12 TCL power-off repair, which Bedroom physically confirmed working.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.12 / Receiver 1.0.0-test.9 — 2026-08-30

Approved Remote-layout and TCL power-off repair candidate.

- Restored individual and group Off to the standard Android `Power` key physically proven on Bedroom in test.6; the TCL-ignored `Sleep` regression is covered by a focused test.
- Rebuilt portrait Remote to match the approved mockup: wide Back/Home/Input controls, a larger centered D-pad with small gaps, Volume and Channel stacks entirely below it, and no VOL/CH labels.
- Changed Channel to up/down caret controls, retained Volume as `+ / mute / −`, and kept all secondary controls smaller than the D-pad.
- Restored the full-width dark-teal playback bar without a label, and anchored centered **Remove this TV** at the bottom just above phone navigation.
- Kept landscape compact while making the D-pad larger than its side controls, the playback strip full-width, and **Remove this TV** centered beneath it.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.

## 1.0.0-test.11 / Receiver 1.0.0-test.9 — 2026-08-29

Physical-feedback repair and compact-layout candidate.

- Fixed individual and group Off commands being skipped when Android TV's app-start notification was incorrectly treated as a reliable TV power-state signal. Off now always sends Sleep after the remote session connects.
- Changed Refresh into an explicit remote disconnect/reconnect followed by Receiver resynchronization, with visible progress and result feedback.
- Rebuilt Remote as a non-scrolling deck: volume `+ / mute / −` sits left of the D-pad, channel `+ / blank / −` sits right, playback sits below, and the remove action remains last. Landscape uses the same controls in a shorter horizontal arrangement.
- Changed Apps to compact circular launcher-style icons with one-line ellipsized labels and responsive columns while retaining per-TV drag ordering.
- Returns Controller to Remote immediately when an app is selected.
- Made Receiver's Connection and Phone Access cards equal size.
- Replaced transparent adaptive-icon backgrounds with the same navy used behind RoomDeck's orange play symbol on Controller and Receiver.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.
## 1.0.0-test.10 / Receiver 1.0.0-test.8 — 2026-08-29

Scope-finalization candidate based on the working Bedroom build.

- Removed unreliable individual/group power-on controls while retaining individual TV Off and **All TVs Off**.
- Removed RoomDeck's USB tab, USB/media permissions, indexing, and playback routes. The TCL Media Player remains launchable from Apps and owns USB browsing, playback, and subtitles.
- Reduced Receiver to Tailscale connection details, phone-access status, reset, restart, and its installed-app listing/launch service.
- Added persistent per-TV hold-and-drag app ordering. Newly installed apps append to the saved order, uninstalled apps disappear, and **Reset order** restores the default alphabetical order.
- Changed Controller and Receiver launcher artwork, plus the Android TV banner, to use a transparent surrounding background.

Physical verification is required on Bedroom and Living Room before V1 can be called release-ready.
## 1.0.0-test.9 / Receiver 1.0.0-test.7 — 2026-08-29

Bedroom-feedback candidate for automatic Receiver recovery, USB playback metadata, and best-effort TV wake.

- Automatically synchronizes Receiver whenever the selected Android TV remote connects, retries transient failures, repairs obsolete Receiver authorization, and preloads installed-app icons before the Apps tab opens.
- Automatically requests Android's audio/video permission when Receiver opens; the TV owner still approves the system prompt once, with the Receiver button retained only as a retry.
- Routes USB playback through a private short-lived content provider whose URI, display name, MIME type, and size are readable by TCL Media Player instead of exposing an opaque MediaStore URI that appeared as `null`.
- Uses Android's explicit Sleep and Wakeup commands instead of a power toggle, plus an authenticated Receiver screen-wake request while Receiver and Tailscale remain reachable in standby.

Bedroom physical verification is required for automatic app loading, the TCL Media Player handoff, and individual/group wake before Living Room testing.
## 1.0.0-test.8 / Receiver 1.0.0-test.6 — 2026-08-29

Bedroom-feedback candidate for reliable manual setup, installed-app launch, and TV-native USB playback.

- Removed nearby Receiver search and mDNS advertising; Add TV now uses only the `100.x` address displayed by Receiver.
- Removed the port suffix and second Receiver code from the TV UI. The Android TV Remote code now automatically authorizes Receiver through a short-lived, one-time TV route.
- Changed app tiles to request a one-time foreground Receiver route that opens the exact installed leanback activity.
- Removed RoomDeck's internal Media3 player and routes USB files to the TV's own media handler with a validated MIME type, display name, and temporary URI grant.
- Made all five playback controls divide the full row in portrait and landscape.
- Replaced the detail-page status pill with a compact icon left of Refresh and centered **Remove this TV** across the row.
- Removed the unused direct-input API after the separate Input tab was retired; the Remote tab's Input button remains.

Bedroom physical verification is required before Living Room testing.
## 1.0.0-test.7 / Receiver 1.0.0-test.5 — 2026-08-29

Bedroom-feedback candidate focused on the final marked UI and practical TV behavior.

- Reduced the TV detail view to Remote, Apps, and USB; input control stays on the Remote tab.
- Replaced clipped Home, Input, and Mute labels with icon controls, prevented room and group-power wrapping, and removed duplicated room/device labels.
- Changed the center playback control to a pause icon that sends the same D-pad Center command proven to toggle playback on the Bedroom TV.
- Changed installed-app launches to Android TV Remote's package-launch URI and added icons read from each TV's app metadata.
- Automatically uses a single attached removable USB volume, loads its metadata on Receiver connection, and sends only the listing to Controller.
- Routes a selected USB item through a one-time Receiver app link into the TV's own media handler instead of RoomDeck's internal player.
- Restored nearby Receiver search using local mDNS metadata that advertises the Tailscale destination; manual Tailscale entry remains available when router isolation blocks discovery.
- Keeps Receiver's foreground service awake during standby as a best-effort improvement for network power-on; TCL firmware can still power down networking independently.

Bedroom physical verification is required before Living Room testing.
## 1.0.0-test.6 / Receiver 1.0.0-test.4 — 2026-08-29

Physical-feedback repair candidate after Tailscale proved that Controller can reach the Android TV 14 Receiver.

- Made Add TV strictly manual through the Receiver-displayed Tailscale address; no discovery action or discovery wording remains in the active UI.
- Switched individual and group power handling to the standard Android TV power command and used the TV's reported power state to avoid unnecessary group toggles.
- Reworked the Remote tab so labels stay on one line and volume is a consistent `−`, **Mute**, `+` row.
- Replaced fixed TCL-ignored HDMI commands with each TV's reported pass-through inputs and added Back, Up, Down, and OK controls beside the input menu.
- Added a Live TV launcher route when the TV exposes a **Live TV** app, retaining a clearly labeled remote-key fallback otherwise.
- Integrated the separate Receiver code prompt into the Add TV flow and routed installed-app launches through Android TV Remote to avoid Android 14 background-launch blocking.
- Replaced the unavailable TV folder picker with scoped audio/video permission plus removable-volume MediaStore browsing.
- Added focused tests for launch intent URIs, Live TV label matching, and opaque USB folder IDs.

Bedroom physical verification is required for power, direct inputs, app launches, and USB indexing before testing Living Room.
## 1.0.0-test.5 / Receiver 1.0.0-test.3 — 2026-08-29

Tailscale-only transport candidate for routers where AP/client isolation cannot be disabled.

- Removed active LAN/mDNS discovery and its Nearby Devices and multicast permissions from Controller.
- Required Controller traffic to use an active Tailscale VPN network and a TV address in Tailscale's `100.64.0.0/10` range; there is no LAN fallback.
- Changed Add TV to use the Tailscale address displayed by Receiver rather than local discovery.
- Changed Receiver to wait for Tailscale and bind its HTTP bridge only to the TV's Tailscale address.
- Updated connection errors and documentation for the Tailscale-only architecture.
- Added focused Tailscale address-range regression tests for Controller and Receiver.

Physical testing must confirm that TCL's Android TV Remote service accepts pairing and control connections through the Tailscale interface. Bedroom verification comes first.

## 1.0.0-test.4 — 2026-08-28

Local-network routing candidate after Bedroom showed that neither Android TV Remote port 6467 nor RoomDeck Receiver port 41234 was reachable from the phone, while the official Google TV remote still worked.

- Added the Nearby Devices permission required by Android's newer local-network protection path.
- Bound Android TV pairing, remote control, and Receiver HTTP traffic to the phone's Wi-Fi network instead of relying on the default route.
- Corrected the Receiver's mDNS service type and changed its displayed endpoint to use the TV's active Wi-Fi or Ethernet network.
- Bumped the Receiver test build because its discovery advertisement and endpoint selection changed.

Bedroom physical verification is required before testing Living Room again.

## 1.0.0-test.3 — 2026-08-28

Bedroom-only pairing compatibility candidate after confirming that the official Google TV phone remote controls the Android TV 14 TCL.

- Replaced the extension-less v3 client identity with a Polo-compatible self-signed RSA v1 certificate.
- Added a one-time migration so installations updated from test.2 actually regenerate the corrected identity.
- Changed the Polo service name to `atvremote` and advertised only the client input encoding used by working implementations.
- Enforced the protocol's six-hex-character pairing code and checksum prefix.
- Split TCP reachability from TLS rejection instead of blaming the physical or Wi-Fi remote setting for every connection failure.
- Added an automatic Receiver reachability check after failed pairing and kept the result visible longer.
- Added certificate and canonical pairing-wire regression tests.

The Receiver APK is unchanged. Bedroom physical verification is required before testing Living Room again.
## 1.0.0-test.2 — 2026-08-28

Replacement controller candidate after the first TCL physical-device discovery test.

- Fixed local mDNS discovery by holding the required Wi-Fi multicast lock while scanning.
- Retained and cleanly unregistered Android 14+ service-info callbacks.
- Added one-shot resolution fallback when Android rejects a service-info callback.
- Added independent TV discovery through an installed RoomDeck Receiver.
- Merged duplicate Android TV and Receiver advertisements into one TV entry.
- Added focused tests for dual-source discovery and Receiver-only fallback.

The Receiver APK is unchanged. Physical pairing and remote-control compatibility remain pending on the target TCL Android TV 12 and Android TV 14 devices.
## 1.0.0-test.1 — 2026-08-28

Initial physical-device test candidate.

- Added multi-TV profiles, selection, and connection state.
- Added Android TV Remote v2 discovery, secure pairing, certificate pinning, reconnect behavior, and remote commands.
- Added direct HDMI 1–4, Live TV, and input-menu commands.
- Added All TVs On and All TVs Off.
- Added a TV Receiver with per-TV pairing and launchable-app discovery.
- Added Android Storage Access Framework USB selection, browsing, and local Media3 playback.
- Added pairing rate limiting, bounded local HTTP requests, bearer authorization, and backup opt-out.
- Added Receiver restart after normal TV reboot.

Physical compatibility is still pending on the target TCL Android TV 12 and Android TV 14 devices.
