# Security policy

## Supported version

Only the latest RoomDeck version is supported. `v1.0.0` is the current private personal release. Android TV 12 and full cross-TV compatibility remain pending in the physical test checklist.

## Distribution boundary

The private V1 APKs retain RoomDeck's development signing identity and are debuggable so they can update the installed physical-test builds without clearing pairing data. They are intended only for Kirk's private devices and are not production-signed distribution binaries.

## Reporting a problem

Do not open a public issue containing pairing codes, access tokens, certificates, private network addresses, or device identifiers. Use a private GitHub security advisory after the repository is published.

## Security model

- Android TV remote pairing uses TLS, client certificates, and a pinned TV certificate.
- Controller requires an active Tailscale VPN route and accepts only IPv4 addresses in Tailscale's 100.64.0.0/10 range.
- Receiver waits for Tailscale and binds its authenticated bridge only to the TV's Tailscale address; there is no direct-LAN fallback or local discovery advertisement.
- Receiver issues a random 256-bit access token only after a short-lived one-time enrollment route is launched through the already-paired Android TV Remote channel.
- Enrollment secrets are high entropy, expire after one minute, and can be consumed only once.
- Receiver requests have bounded headers and bodies, short timeouts, and no caching. Only device status and the two-step enrollment handshake are unauthenticated; app listing and launch routes require the per-TV token.
- Receiver has no USB or media-library permission and exposes no media endpoint.
- Both Android apps opt out of backup.

Receiver's application protocol is authenticated HTTP carried inside Tailscale's encrypted tunnel. Tailnet membership is not a substitute for Receiver authentication: keep Tailscale access policies restricted, leave unknown devices out of the tailnet, and rotate Receiver access after any suspected exposure. RoomDeck depends on Tailscale's availability and security for transport.
