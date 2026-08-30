# Privacy

RoomDeck is designed for personal use through a private Tailscale network (tailnet).

## Data RoomDeck stores

The Controller stores, on the phone:

- room labels and TV names;
- TV Tailscale addresses and remote-control ports;
- Android TV pairing certificates and server certificate pins;
- one random Receiver access token for each paired TV; and
- the saved app-tile order for each TV.

The Receiver stores, on its TV:

- a random device identifier; and
- one random Controller access token.

Android app sandbox storage is used. RoomDeck disables Android backup for both apps so pairing material is not copied into cloud backups.

## Network behavior

RoomDeck itself does not provide accounts, analytics, advertising, telemetry, or a cloud relay. It requires the separately installed Tailscale app and a Tailscale account or identity provider. Commands travel between the phone and TVs through the private tailnet; Tailscale's own service and privacy terms apply to that transport.

The standard Android TV remote-control connection is encrypted and uses a pinned TV certificate after pairing. The separate Receiver bridge uses authenticated HTTP inside the encrypted Tailscale tunnel. Receiver binds only to the TV's Tailscale address, and Controller refuses LAN addresses or a non-Tailscale route. Receiver does not advertise itself through local discovery. After Android TV Remote pairing, Controller sends a random short-lived enrollment secret to Receiver and proves control by launching that one-time route on the TV; Receiver then issues its random per-TV token. Tailnet access policies and this token provide separate access layers.

## TV apps and USB media

Receiver reads only Android's launchable-app metadata needed for the Apps tab: label, package name, and icon. RoomDeck does not request media-library permissions, index attached drives, receive USB filenames, or copy media to the phone. When the TV's own Media Player is opened from Apps, Android TV and that app handle USB browsing and playback independently of RoomDeck.

## Removing access

- Remove a TV in Controller to delete that TV's local profile, saved app order, Receiver token, and Android TV certificate pin from the phone.
- Choose **Reset phone access** in Receiver to invalidate its Controller token. Pair the TV again in Controller to authorize a new token.
- Clear either app's data in Android settings to remove all RoomDeck state stored by that app.
