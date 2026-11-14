# TeslaCast Network PoC

This is a **network diagnostic app**, not a screen mirroring app. It hosts a simple HTTP test page on port 9090 bound to 0.0.0.0.

## Build APK
GitHub Actions > Build TeslaCast diagnostic APK > Run workflow. Download the artifact after success.

## Test
1. Install APK on Samsung Android phone and start its hotspot.
2. Connect Tesla to the hotspot.
3. Tap START LOCAL TEST SERVER and note the hotspot IPv4 address.
4. Verify the URL on another phone, then on Tesla browser.
5. Report exact URL and error. No VPN or Tesla bypass is implemented yet.

No cloud relay or paid server is used. GitHub Actions is only for compilation.
