# ByeDPI for Android

**English** | [Русский](README-ru.md)

<div style="text-align: center;">
  <img alt="ByeDPI logo" src=".github/images/logo.svg" width="100%" height="200px">
</div>

---

Android application that runs a local VPN service to bypass DPI (Deep Packet Inspection) and censorship.


This application runs a SOCKS5 proxy [ByeDPI](https://github.com/hufrea/byedpi) and redirects all traffic through it.

## Installation

[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/dovecoteescapee/ByeDPIAndroid/releases)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
    alt="Get it on IzzyOnDroid"
    height="80">](https://apt.izzysoft.de/fdroid/index/apk/io.github.dovecoteescapee.byedpi)

### Or use Obtainium

1. Install [Obtainium](https://github.com/ImranR98/Obtainium/blob/main/README.md#installation)
2. Add the app by URL:  
   `https://github.com/dovecoteescapee/ByeDPIAndroid`

## Settings

To bypass some blocks, you may need to change the settings. More about the various settings can be found in the [ByeDPI documentation](https://github.com/hufrea/byedpi/blob/v0.13/README.md).

## FAQ

### I can't configure it. What to do?

You can ask for help in [discussion](https://github.com/dovecoteescapee/ByeDPIAndroid/discussions).

### Does the application require root access?

No. All application features work without root.

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

[Similar projects](https://github.com/ValdikSS/GoodbyeDPI/blob/master/README.md#similar-projects))

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
