# RoomDeck V1 TV test checklist

Release baseline: Controller `v1.0.0` (behavior from test.18) and Receiver `v1.0.0` (behavior from test.10).

Use the same Controller build for both TVs. Install the same Receiver build on each TV.

Record only pass/fail and a short symptom. Do not commit private addresses, pairing codes, tokens, device identifiers, or app-account details.

Before testing each TV, connect Tailscale on the phone and TV to the same tailnet, leave incoming connections enabled on the TV, and confirm Receiver shows **Ready**. Do not record its address.

## Living Room — TCL Android TV 12 (`V8-0013T02...`)

- [ ] Add TV accepts the `100.x` address displayed by Receiver; no nearby-search section appears.
- [ ] The Android TV Remote code completes both remote pairing and automatic Receiver authorization; no second code is requested.
- [ ] Receiver shows equal-size **Connection** and **Phone Access** cards, **Ready**, the Tailscale address without a port, and **Phone Access: Connected**.
- [ ] Home, Back, Input, D-pad, volume up/down, mute, channel up/down, and playback controls work; Remote fits without scrolling and the compact connection icon sits left of Refresh.
- [ ] Input opens the TV chooser; Back exits it, and D-pad plus OK navigate it correctly; no separate Input tab appears.
- [ ] Apps shows compact circular icons with one-line ellipsized names; launching at least two apps immediately returns Controller to Remote.
- [ ] Hold-drag rearranges app tiles, the order survives leaving the TV and restarting Controller, and **Reset order** restores the default order.
- [ ] The TV's Media Player appears in Apps and opens; its own interface can browse the attached USB drive and use its normal subtitle controls.
- [ ] No USB tab, individual On, or All TVs On control appears; Controller and Receiver icons use the same navy background as the play-symbol rectangle.
- [ ] The Remote tab's five playback controls fill the row in portrait and landscape; Pause toggles playback through the same command as OK.
- [ ] **Remove this TV** is centered in portrait and landscape.
- [ ] Individual TV Off works, **All TVs Off** sends to this TV, and Refresh visibly reconnects the remote.
- [ ] After a normal TV reboot, Receiver becomes reachable without opening it manually.

## Bedroom — TCL Android TV 14 (`V8-0012T01...`)

- [ ] Add TV accepts the `100.x` address displayed by Receiver; no nearby-search section appears.
- [ ] The Android TV Remote code completes both remote pairing and automatic Receiver authorization; no second code is requested.
- [ ] Receiver shows equal-size **Connection** and **Phone Access** cards, **Ready**, the Tailscale address without a port, and **Phone Access: Connected**.
- [ ] Home, Back, Input, D-pad, volume up/down, mute, channel up/down, and playback controls work; Remote fits without scrolling and the compact connection icon sits left of Refresh.
- [ ] Input opens the TV chooser; Back exits it, and D-pad plus OK navigate it correctly; no separate Input tab appears.
- [ ] Apps shows compact circular icons with one-line ellipsized names; launching at least two apps immediately returns Controller to Remote.
- [ ] Hold-drag rearranges app tiles, the order survives leaving the TV and restarting Controller, and **Reset order** restores the default order.
- [ ] The TV's Media Player appears in Apps and opens; its own interface can browse the attached USB drive and use its normal subtitle controls.
- [ ] No USB tab, individual On, or All TVs On control appears; Controller and Receiver icons use the same navy background as the play-symbol rectangle.
- [ ] The Remote tab's five playback controls fill the row in portrait and landscape; Pause toggles playback through the same command as OK.
- [ ] **Remove this TV** is centered in portrait and landscape.
- [ ] Individual TV Off works, **All TVs Off** sends to this TV, and Refresh visibly reconnects the remote.
- [ ] After a normal TV reboot, Receiver becomes reachable without opening it manually.

## Cross-TV isolation

- [ ] Selecting Living Room changes only the Living Room TV.
- [ ] Selecting Bedroom changes only the Bedroom TV.
- [ ] Each Apps tab reflects that TV's own installed apps, icons, and saved tile order.
- [ ] Installing a new TV app appends it to that TV's existing saved order; uninstalling an app removes its tile.
- [ ] **All TVs Off** reaches both TVs.
