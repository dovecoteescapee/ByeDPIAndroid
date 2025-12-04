<div align="center">
  <p>
    <img src="https://github.com/romanvht/ByeDPIAndroid/raw/master/.github/images/app.svg" alt="Логотип ByeDPI" width="200" />
  </p>
  <h1>ByeByeDPI Android</h1>
  <p>
    <a href="README.md">Русский</a> |
    <a href="README-en.md">English</a> |
    Türkçe
  </p>
  <p>
    <a href="https://github.com/romanvht/ByeByeDPI/releases/latest"><img src="https://img.shields.io/github/v/release/romanvht/ByeByeDPI" alt="Latest Release" /></a>
    <a href="https://github.com/romanvht/ByeByeDPI/releases"><img src="https://img.shields.io/github/downloads/romanvht/ByeByeDPI/total" alt="Downloads" /></a>
    <a href="https://github.com/romanvht/ByeByeDPI/blob/master/LICENSE"><img src="https://img.shields.io/github/license/romanvht/ByeByeDPI" alt="License" /></a>
  </p>
</div>

ByeDPI'yi yerel olarak çalıştıran ve tüm trafiği bunun üzerinden yönlendiren bir Android uygulaması.

Kararlı bir çalışma için ayarları yapmanız gerekebilir. Farklı ayarlar hakkında daha fazla bilgiye [ByeDPI dökümantasyonundan](https://github.com/hufrea/byedpi/blob/v0.13/README.md) ulaşabilirsiniz.

Bu uygulama **VPN** değildir. Trafiği yönlendirmek için Android'in VPN modunu kullanır ancak herhangi bir veriyi uzak bir sunucuya iletmez. Trafiği şifrelemez veya IP adresinizi gizlemez.

Bu uygulama, [ByeDPIAndroid](https://github.com/dovecoteescapee/ByeDPIAndroid) uygulamasının bir çatallamasıdır.

---

### Özellikler
* Cihaz başlatıldığında hizmetin otomatik başlatılması
* Komut satırı parametrelerinin listelerinin kaydedilmesi
* Android TV/BOX ile geliştirilmiş uyumluluk
* Uygulama başına bölünmüş tünelleme
* Ayarları içe/dışa aktarma

### Kullanım
* Otomatik başlatmayı etkinleştirmek için ayarlarda seçeneği aktifleştirin.
* İlk başta VPN'e bağlanarak isteği kabul etmeniz önerilir.
* Bundan sonra, cihaz başlatıldığında, uygulama ayarlara göre (VPN/Proxy) hizmeti otomatik olarak başlatacaktır.
* Android TV/BOX kullanıyorsanız ve Ethernet bağlantınız VPN'e bağlanırken kopuyorsa, beyaz liste modunu etkinleştirip VPN üzerinden çalışması gereken uygulamaları belirleyin (örneğin, Discord).
* Topluluktan kapsamlı talimatlar [ByeByeDPI-Manual (İngilizce)](https://github.com/BDManual/ByeByeDPI-Manual)

### Türkiye İle İlgili
* Uygulama Türkiyede uygulanan DPI'ı aşmak için şuanlık yeterlidir. İlk başta çalıştırdığınızda uygulama DPI'ı aşamayabilir. UI editöründen rastgele taktikler deneyebilirsiniz veya şuan Deneysel özellik olan proxy modundan bazı argümanlar alıp onları Komut satırı editöründe deneyebilirsiniz.
* Türkiye ile alakalı destek için Discord: [nyaex](https://github.com/nyaexx)


### ByeByeDPI'yi AdGuard ile nasıl kullanırım?
* ByeByeDPI'yi proxy modunda başlatın.
* ByeByeDPI'yi AdGuard dışlamalarına "Uygulama Yönetimi" sekmesinde ekleyin.
* AdGuard ayarlarında, proxy'i belirtin:
```plaintext
Proxy Türü: SOCKS5
Host: 127.0.0.1
Port: 1080 (varsayılan)
```

### Oluşturma
1. Depoyu alt modüllerle klonlayın:
```bash
git clone --recurse-submodules
```
2. Depo kökünden derleme betiğini çalıştırın:
```bash
./gradlew assemblyRelease
```
3. APK `app/build/outputs/apk/release/` dizininde olacaktır

> Not: hev_socks5_tunnel Windows altında derlenmeyecektir, WSL kullanmanız gerekecektir

### İmza Özeti
SHA-256:
`77:45:10:75:AC:EA:40:64:06:47:5D:74:D4:59:88:3A:49:A6:40:51:FA:F3:2E:42:F7:18:F3:F9:77:7A:8D:FB`

### Bağımlılıklar
- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)
