# ByeDPI for Android

**English** | [Русский](README-ru.md)

<div style="text-align: center;">
  <img src=".github/images/logo.svg" width="100%" height="200px">
</div>

---

Android application that runs a local VPN service to bypass DPI (Deep Packet Inspection) and censorship.


This application runs a SOCKS5 proxy [ByeDPI](https://github.com/hufrea/byedpi) and redirects all traffic through it.

## Installation

[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/dovecoteescapee/ByeDPIAndroid/releases)

### Or use Obtainium

1. Install [Obtainium](https://github.com/ImranR98/Obtainium?tab=readme-ov-file#installation)
2. Add the app by URL:  
   `https://github.com/dovecoteescapee/ByeDPIAndroid`

## Settings

To bypass some blocks, you may need to change the settings. More about the various settings can be found in the [ByeDPI documentation](https://github.com/hufrea/byedpi#readme-ov-file).

## FAQ

### I can't configure it. What to do?

You can ask for help in [discussion](https://github.com/dovecoteescapee/ByeDPIAndroid/discussions).

### Does the application require root access?

No. All application features work without root.

### How to get updates?

Use [Obtainium](#or-use-obtainium).

### Is this a VPN?

No. The application uses the VPN mode on Android to redirect traffic, but does not send anything to a remote server. It does not encrypt traffic and does not hide your IP address.

### How to use ByeDPI with AdGuard?

1. Run ByeDPI in proxy mode.
2. Add ByeDPI to AdGuard exceptions on the "App management" tab.
3. In AdGuard settings, specify the proxy:

   ```plaintext
   Proxy type: SOCKS5
   Proxy host: 127.0.0.1
   Proxy port: 1080 (default)
   ```

### What data does the application collect?

None. The application does not send any data to a remote server. All traffic is processed on the device.

### Are there any for other platforms?

For Windows, there is [GoodByeDPI](https://github.com/ValdikSS/GoodbyeDPI), and for Linux and macOS [zapret](https://github.com/bol-van/zapret). Also, you can use the original [ByeDPI](https://github.com/hufrea/byedpi) to run a similar proxy on Windows, Linux, or macOS.

[Other similar projects](https://github.com/ValdikSS/GoodbyeDPI?tab=readme-ov-file#similar-projects)

### What is DPI?

DPI (Deep Packet Inspection) is a technology for analyzing and filtering traffic. It is used by providers and government agencies to block sites and services.

## Dependencies

- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)

## Building

For building the application, you need:

1. JDK 8 or later
2. Android SDK
3. Android NDK
4. CMake 3.22.1 or later

To build the application:

1. Clone the repository with submodules:
   ```bash
   git clone --recurse-submodules
   ```
2. Run the build script from the root of the repository:
   ```bash
   ./gradlew assembleRelease
   ```
3. The APK will be in `app/build/outputs/apk/release/`
