<div align="center">
  <p>
    <img src="https://github.com/romanvht/ByeDPIAndroid/raw/master/.github/images/app.svg" alt="Логотип ByeDPI" width="200" />
  </p>
  <h1>ByeByeDPI Android</h1>
  <p>
    <a href="README.md">Русский</a> |
    English |
    <a href="README-tr.md">Türkçe</a>
  </p>
  <p>
    <a href="https://github.com/romanvht/ByeByeDPI/releases/latest"><img src="https://img.shields.io/github/v/release/romanvht/ByeByeDPI" alt="Latest Release" /></a>
    <a href="https://github.com/romanvht/ByeByeDPI/releases"><img src="https://img.shields.io/github/downloads/romanvht/ByeByeDPI/total" alt="Downloads" /></a>
    <a href="https://github.com/romanvht/ByeByeDPI/blob/master/LICENSE"><img src="https://img.shields.io/github/license/romanvht/ByeByeDPI" alt="License" /></a>
  </p>
</div>

An Android application that locally runs ByeDPI and routes all traffic through it.

For stable operation, you may need to adjust the settings. You can read more about different settings in the [ByeDPI documentation](https://github.com/hufrea/byedpi/blob/v0.13/README.md).

This application is **not** a VPN. It uses Android's VPN mode to route traffic but does not transmit anything to a remote server. It does not encrypt traffic or hide your IP address.

This application is a fork of [ByeDPIAndroid](https://github.com/dovecoteescapee/ByeDPIAndroid).

---

### Features
* Autostart service on device boot
* Saving lists of command-line parameters
* Improved compatibility with Android TV/BOX
* Per-app split tunneling
* Import/export settings

### Usage
* To enable auto-start, activate the option in settings.
* It is recommended to connect to the VPN once to accept the request.
* After that, upon device startup, the application will automatically launch the service based on settings (VPN/Proxy).
* If you are using an Android TV/BOX and your Ethernet connection drops when connecting, enable whitelist mode and specify the apps that should work through the VPN (e.g., YouTube).
* Comprehensive instruction from the community [ByeByeDPI-Manual (En)](https://github.com/BDManual/ByeByeDPI-Manual)

### How to use ByeByeDPI with AdGuard?
* Start ByeByeDPI in proxy mode.
* Add ByeByeDPI to AdGuard exclusions on the "App Management" tab.
* In AdGuard settings, specify the proxy:
```plaintext
Proxy Type: SOCKS5
Host: 127.0.0.1
Port: 1080 (default)
```

### Building
1. Clone the repository with submodules:
   ```bash
   git clone --recurse-submodules
   ```
2. Run the build script from the root of the repository:
   ```bash
   ./gradlew assembleRelease
   ```
3. The APK will be in `app/build/outputs/apk/release/`

> P.S.: hev_socks5_tunnel will not build under Windows, you will need to use WSL

### Signature Hash
SHA-256:
`77:45:10:75:AC:EA:40:64:06:47:5D:74:D4:59:88:3A:49:A6:40:51:FA:F3:2E:42:F7:18:F3:F9:77:7A:8D:FB`

### Dependencies
- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)
