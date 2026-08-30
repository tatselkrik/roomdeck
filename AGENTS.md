# RoomDeck project instructions

- Preserve the V1 product boundary: multiple TCL Android TVs, ordinary remote controls with the TV input chooser, per-TV installed-app listing, launch, and saved drag ordering, individual TV Off, and only All TVs Off as a group action. Power-on and RoomDeck USB browsing are intentionally excluded.
- Do not add phone-media casting, playback transfer, cloud services, accounts, analytics, advertising, or automation scenes without explicit approval.
- Keep application IDs `io.github.tatselkrik.roomdeck` and `io.github.tatselkrik.roomdeck.receiver`.
- Keep Receiver access isolated per TV and never log pairing codes, bearer tokens, certificates, installed-app inventories, or private network addresses in release builds.
- Keep adapted ScreenCast notices intact and update `THIRD_PARTY_NOTICES.md` when adaptation scope changes.
- Before a test handoff, run `testDebugUnitTest`, `lintDebug`, and `assembleDebug` for the exact source state.
- The TCL Android TV 12 and Android TV 14 physical checklist in `docs/TV_TEST_CHECKLIST.md` is required before calling V1 release-ready.
- Do not publish, tag `v1.0.0`, or create a GitHub release until Kirk approves the tested behavior and the exact pushed commit passes GitHub Actions.
