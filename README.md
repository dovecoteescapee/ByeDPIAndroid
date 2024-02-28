# ByeDPI for Android

**English** | [Русский](README-ru.md)

<div align="center">
<img src=".github/images/logo.svg" height="200px" width="200px" />
</div>

---

Android application that runs a local VPN service to bypass DPI (Deep Packet Inspection) and censorship.

The application locally runs [ByeDPI](https://github.com/hufrea/byedpi) and redirects all TCP traffic through it.

## Quick Start

1. Download the .apk file from the [releases](https://github.com/dovecoteescapee/ByeDPIAndroid/releases/latest)
2. Install
3. Run and press Connect

## Settings

To bypass some blocks, you may need to change the settings. More about the various settings can be found in the [ByeDPI documentation](https://github.com/hufrea/byedpi#readme-ov-file).

## FAQ

### Is this a VPN?

No. The application uses the VPN mode on Android to redirect traffic, but does not send anything to a remote server. It does not encrypt traffic and does not hide your IP address.

### What data does the application collect?

None. The application does not send any data to a remote server. All traffic is processed on the device.

### Are there any for other platforms?

For Windows, there is [GoodByeDPI](https://github.com/ValdikSS/GoodbyeDPI), and for Linux and macOS [zapret](https://github.com/bol-van/zapret). Also, you can use the original [ByeDPI](https://github.com/hufrea/byedpi) to run a similar proxy on Windows, Linux, or macOS.

[Other similar projects](https://github.com/ValdikSS/GoodbyeDPI?tab=readme-ov-file#similar-projects)

### What is DPI?

DPI (Deep Packet Inspection) is a technology for analyzing and filtering traffic. It is used by providers and government agencies to block sites and services.

## Dependencies

- [ByeDPI](https://github.com/hufrea/byedpi)
- [Tun2Socks](https://github.com/dovecoteescapee/tun2socks)*  
  *fork with the addition of separate tunneling of TCP and UDP
